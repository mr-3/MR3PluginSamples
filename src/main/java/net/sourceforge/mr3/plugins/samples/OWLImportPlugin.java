/*
 * @(#) OWLImportPlugin.java
 * 
 * Copyright (C) 2003 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */

package net.sourceforge.mr3.plugins.samples;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.swing.JFileChooser;

import net.sourceforge.mr3.plugin.MR3Plugin;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Takeshi Morita
 */
public class OWLImportPlugin extends MR3Plugin {

	private Model convertOntModelToRDFSModel(OntModel ontModel) {
		Model rdfsModel = ModelFactory.createDefaultModel();
		rdfsModel.setNsPrefixes(ontModel.getNsPrefixMap());
		addClassModel(ontModel, rdfsModel);
		addObjectPropertyModel(ontModel, rdfsModel);
		addDatatypePropertyModel(ontModel, rdfsModel);

		// StringWriter out = new StringWriter();
		// rdfsModel.write(new PrintWriter(out), "RDF/XML-ABBREV");
		// System.out.println(out.toString());

		return rdfsModel;
	}

	private void addType(OntResource ontRes, Resource type, Model rdfsModel) {
		Statement stmt = rdfsModel.createStatement(ontRes, RDF.type, type);
		rdfsModel.add(stmt);
	}

	private void addComments(OntResource ontRes, Model rdfsModel) {
		for (Iterator i = ontRes.listComments(null); i.hasNext();) {
			Literal literal = (Literal) i.next();
			Statement stmt = rdfsModel.createStatement(ontRes, RDFS.comment, literal);
			rdfsModel.add(stmt);
		}
	}

	private void addLabels(OntResource ontRes, Model rdfsModel) {
		for (Iterator i = ontRes.listLabels(null); i.hasNext();) {
			Literal literal = (Literal) i.next();
			Statement stmt = rdfsModel.createStatement(ontRes, RDFS.comment, literal);
			rdfsModel.add(stmt);
		}
	}

	private void addDomains(OntProperty ontProp, Model rdfsModel) {
		for (Iterator i = ontProp.listDomain(); i.hasNext();) {
			OntResource res = (OntResource) i.next();
			Statement stmt = rdfsModel.createStatement(ontProp, RDFS.domain, res);
			rdfsModel.add(stmt);
		}
	}

	private void addRanges(OntProperty ontProp, Model rdfsModel) {
		for (Iterator i = ontProp.listRange(); i.hasNext();) {
			OntResource res = (OntResource) i.next();
			Statement stmt = rdfsModel.createStatement(ontProp, RDFS.range, res);
			rdfsModel.add(stmt);
		}
	}

	private void addSubClassOf(OntResource ontRes, OntResource ontSupRes, Model rdfsModel) {
		Statement stmt = rdfsModel.createStatement(ontRes, RDFS.subClassOf, ontSupRes);
		rdfsModel.add(stmt);
		addType(ontRes, OWL.Class, rdfsModel);
	}

	private void addSubPropertyOf(OntResource ontRes, OntResource ontSupRes, Model rdfsModel) {
		Statement stmt = rdfsModel.createStatement(ontRes, RDFS.subPropertyOf, ontSupRes);
		rdfsModel.add(stmt);
		addType(ontRes, OWL.ObjectProperty, rdfsModel);
	}

	private void addClassModel(OntModel ontModel, Model rdfsModel) {
		for (Iterator i = ontModel.listNamedClasses(); i.hasNext();) {
			OntClass ontClass = (OntClass) i.next();
			addType(ontClass, OWL.Class, rdfsModel);
			addLabels(ontClass, rdfsModel);
			addComments(ontClass, rdfsModel);

			for (Iterator j = ontClass.listSubClasses(true); j.hasNext();) {
				OntClass subOntClass = (OntClass) j.next();
				addSubClassOf(subOntClass, ontClass, rdfsModel);
			}
		}
	}

	private void addPropertyModel(OntProperty ontProp, Model rdfsModel) {
		addLabels(ontProp, rdfsModel);
		addComments(ontProp, rdfsModel);
		addDomains(ontProp, rdfsModel);
		addRanges(ontProp, rdfsModel);
		for (Iterator i = ontProp.listSubProperties(true); i.hasNext();) {
			OntProperty subOntProp = (OntProperty) i.next();
			if (!subOntProp.equals(ontProp)) {
				addSubPropertyOf(subOntProp, ontProp, rdfsModel);
			} else {
				// System.out.println("Property: " + ontProp);
				// System.out.println("SubProperty: " + subOntProp);
			}
		}
	}

	private void addObjectPropertyModel(OntModel ontModel, Model rdfsModel) {
		for (Iterator i = ontModel.listObjectProperties(); i.hasNext();) {
			OntProperty ontProp = (OntProperty) i.next();
			addType(ontProp, OWL.ObjectProperty, rdfsModel);
			addPropertyModel(ontProp, rdfsModel);
		}
	}

	private void addDatatypePropertyModel(OntModel ontModel, Model rdfsModel) {
		for (Iterator i = ontModel.listDatatypeProperties(); i.hasNext();) {
			OntProperty ontProp = (OntProperty) i.next();
			addType(ontProp, OWL.DatatypeProperty, rdfsModel);
			addPropertyModel(ontProp, rdfsModel);
		}
	}

	public void exec() {
		try {
			InputStream is = getInputStream();
			if (is != null) {
				OntModel ontModel = ModelFactory.createOntologyModel();
				ontModel.read(is, getBaseURI());
				mergeRDFSModel(convertOntModelToRDFSModel(ontModel));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private InputStream getInputStream() throws IOException {
		JFileChooser jfc = new JFileChooser();
		if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			return new BufferedInputStream(new FileInputStream(jfc.getSelectedFile()));
		}
		return null;

	}
}
