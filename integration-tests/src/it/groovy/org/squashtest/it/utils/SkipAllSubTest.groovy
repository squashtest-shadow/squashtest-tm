package org.squashtest.it.utils

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.SpecInfo

class SkipAllSubTest extends AbstractAnnotationDrivenExtension<SkipAll>{


	public void visitSpecAnnotation(SkipAll ignore, SpecInfo spec) {

		def childSpecs = new ArrayList<SpecInfo>();
		SpecInfo curr = spec;
		while (curr != null) {
			childSpecs.add(curr);
			curr = curr.getSubSpec();
		}

		childSpecs.each{SpecInfo info -> info.setSkipped(true)};
	}



	public void visitFeatureAnnotation(SkipAll ignore, FeatureInfo feature) {
		feature.setSkipped(true);
	}
}
