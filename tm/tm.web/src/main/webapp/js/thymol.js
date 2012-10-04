/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
/*-------------------- Thymol - the flavour of Thymeleaf --------------------*

   Thymol version 0.1.1-SNAPSHOT Copyright 2012 James J. Benson.
   jjbenson .AT. users.sf.net

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" basis,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 *---------------------------------------------------------------------------*/

var thURL = "http://www.thymeleaf.org";
var thPrefix = "th";
var thCache = new Object;

$(function() {
	thymol();
});

var thymol = function() {

	var urlParams = {};
	(function() {
		var e, a = /\+/g, r = /([^&=]+)=?([^&]*)/g, d = function(s) {
			return decodeURIComponent(s.replace(a, " "));
		}, f = function(s) {
			return new Param(d(s));
		}, q = window.location.search.substring(1);
		while (e = r.exec(q)) {
			urlParams[d(e[1])] = f(e[2]);
		}
	})();

	var debug = false;

	var thDebugParam = urlParams["thDebug"];
	if (thDebugParam) {
		debug = thDebugParam.getBooleanValue();
	}
	else {
		try {			
			if( !(typeof thDebug === "undefined") ) {
				if ( thDebug != null) {
					debug = (thDebug==true);
				}								
			}
		}
		catch (err) {
		}				
	}

	$.ajaxSetup({
		async : false
	});

	(function() {
		var htmlTag = $("html")[0];
		$(htmlTag.attributes).each(function() {
			if (thURL == this.value) {
				var nsspec = this.localName.split(":");
				if (nsspec.length > 0) {
					thPrefix = nsspec[nsspec.length - 1];
					return;
				}
			}
		});
	})();

	var thIncl = new ThObj("include");
	var thSubs = new ThObj("substituteby");
	var thIf = new ThObj("if");
	var thUnless = new ThObj("unless");
	var thSwitch = new ThObj("switch");
	var thCase = new ThObj("case");

	var thFragEscp = "[" + thPrefix + "\\:fragment='";
	var root = new ThNode(document, false, null, null, null, document.nodeName, "::", false, document);
	process(root);

	function process(root) {
		var n = root;
		while (n.thDoc) {
			getChildren(n);
			if (n.firstChild && n.firstChild.thDoc && !n.visited) {
				n.visited = true;
				n = n.firstChild;
			}
			else {
				doReplace(n.isNode, n.element, n.thDoc);
				if (n.nextSibling && n.nextSibling.thDoc) {
					n = n.nextSibling;
				}
				else {
					if (n == root)
						break;
					else {
						n = n.parentDoc;
					}
				}
			}
		}
	}

	function getChildren(base) {
		var thIfSpecs = $(thIf.escp, base.thDoc);
		var thUnlessSpecs = $(thUnless.escp, base.thDoc);
		var thSwitchSpecs = $(thSwitch.escp, base.thDoc);
		var ths = $(thIfSpecs).add(thUnlessSpecs).add(thSwitchSpecs);
		ths.each(function() {
			var element = this;
			$(element.attributes).each(function() {
				var thAttr = this;
				if (thIf.name == thAttr.name || thUnless.name == thAttr.name || thSwitch.name == thAttr.name) {
					processConditional(element, base, thAttr);
				}
			});
		});

		var thInclSpecs = $(thIncl.escp, base.thDoc);
		var thSubsSpecs = $(thSubs.escp, base.thDoc);
		ths = $(thInclSpecs).add(thSubsSpecs);
		var count = 0;
		var last = null;
		ths.each(function() {
			var element = this;
			$(element.attributes).each(function() {
				var thAttr = this;
				if (thIncl.name == thAttr.name || thSubs.name == thAttr.name) {
					var child = processImport(element, base, thAttr);
					if( child != null ) {
						if (count == 0) {
							base.firstChild = child;
						}
						else {
							last.nextSibling = child;
						}
						last = child;
						count++;						
					}
				}
			});
		});
	}

	function processConditional(element, base, attr) {
		var args = attr.value.match(/[$\*#]{(!?.*)}/);
		var processed = false;
		if (args.length > 0) {
			var param = args[1];
			if (thSwitch.name == attr.name) {
				processed = processSwitch(element, base, attr, param);
			}
			else {
				var negate = false;
				if (args[1].charAt(0) == '!') {
					negate = true;
					param = args[1].substring(1);
				}
				;
				if ((!negate && isTrue(param)) || (negate && !isTrue(param))) {
					if (thUnless.name == attr.name) { // true for "if" and
						// false for "unless"
						element.innerHTML = "";
					}
					processed = true;
				}
				else {
					if (thIf.name == attr.name) { // false for "if", true for
						// "unless"
						element.innerHTML = "";
					}
					processed = true;
				}

			}
		}
		if (!processed && debug) {
			window.alert("thymol.processConditional cannot process: " + attr.name + "=\"" + attr.value + "\"\n" + element.innerHTML);
		}
		element.removeAttribute(attr.name);
	}

	function processSwitch(element, base, attr, param) {
		var matched = false;
		var haveDefault = false;
		var thCaseSpecs = $(thCase.escp, element);
		thCaseSpecs.each(function() {
			var caseClause = this;
			var remove = true;
			$(caseClause.attributes).each(function() {
				var ccAttr = this;
				if (thCase.name == ccAttr.name) {
					if (!matched) {
						var s = urlParams[param];
						if (ccAttr.value == "*" || (s && (s.getStringValue() == ccAttr.value))) {
							matched = true;
							remove = false;
						}
					}
					caseClause.removeAttribute(ccAttr.name);
				}
			});
			if (remove) {
				caseClause.innerHTML = "";
			}
		});
		return matched;
	}

	function processImport(element, base, attr) {
		var importNode = null;
		var filePart = null;
		var fragmentPart = "::";
		if (attr.value.indexOf("::") < 0) {
			filePart = substitute(attr.value);
		}
		else {
			var names = attr.value.split("::");
			filePart = substitute(names[0].trim());
			fragmentPart = substitute(names[1].trim());
		}
		var isNode = (thSubs == attr.localName);
		if (thCache[filePart] != null && thCache[filePart][fragmentPart] != null) {
			isNode = ((thSubs == attr.localName) || (fragmentPart == "::"));
			importNode = new ThNode(thCache[filePart][fragmentPart], false, base, null, null, filePart, fragmentPart, isNode, element);
		}
		else {
			var fileName = filePart + ".html";
			$.get(fileName, function(content, status) {
				if ("success" == status) {
					if (thCache[filePart] == null) {
						thCache[filePart] = new Object;
					}
					if (fragmentPart == "::") {
						var htmlContent = $("html", content)[0];
						thCache[filePart][fragmentPart] = htmlContent;
					}
					else {
						var fragSpec = thFragEscp + fragmentPart + "']";
						var fragArray = $(fragSpec, content);
						$(fragArray).each(function() {
							thCache[filePart][fragmentPart] = this;
						});
					}
					importNode = new ThNode(thCache[filePart][fragmentPart], false, base, null, null, filePart, fragmentPart, isNode, element);
				}
				else if (debug) {
					window.alert("file read failed: " + filePart + " fragment: " + fragmentPart);
				}
			}, "xml");
			if (importNode == null && debug) {
				window.alert("fragment import failed: " + filePart + " fragment: " + fragmentPart);
			}
		}
		element.removeAttribute(attr.name);		
		return importNode;
	}

	function doReplace(isNode, element, content) {
		if (isNode) {
			element.parentNode.replaceChild(content.cloneNode(true), element);
		}
		else {
			element.innerHTML = content.innerHTML;
		}
	}

	function ThNode(thDoc, visited, parentDoc, firstChild, nextSibling, fileName, fragName, isNode, element) {
		this.thDoc = thDoc;
		this.visited = visited;
		this.parentDoc = parentDoc;
		this.firstChild = firstChild;
		this.nextSibling = nextSibling;
		this.fileName = fileName;
		this.fragName = fragName;
		this.isNode = isNode;
		this.element = element;
	}

	function ThObj(suffix) {
		this.name = thPrefix + ":" + suffix;
		this.escp = "[" + thPrefix + "\\:" + suffix + "]";
	}

	function Param(valueArg) {
		this.value = valueArg;
		this.getBooleanValue = function() {
			return !(this.value == "false" || this.value == "off" || this.value == "no");
		};
		this.getStringValue = function() {
			return this.value;
		};
		this.getNumericValue = function() {
			return Number(this.value);
		};
	}

	function isTrue(arg) {
		var p = urlParams[arg];
		if (p) {
			return p.getBooleanValue();
		}
		return false;
	}
	
	function substitute(argValue) {
		var result = argValue;
		var args = argValue.match(/[$\*#]{(!?.*)}/);
		if (args != null && args.length > 0) {
			var param = args[1];
			if(param) {
				var paramValue = urlParams[param];
				if (paramValue) {
					result = paramValue.value;
				}					
			}		
		}			
		return result;
	}

};
