/**
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
package org.squashtest.csp.tm.internal.service

import javax.inject.Inject;

import org.hibernate.LazyInitializationException;
import org.spockframework.util.NotThreadSafe;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.attachment.Attachment;
import org.squashtest.csp.tm.domain.attachment.AttachmentContent;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.hibernate.mapping.HibernateMappingSpecification;
import org.squashtest.csp.tm.service.AttachmentManagerService;
import org.squashtest.csp.tm.service.TestCaseLibrariesCrudService;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;
import org.squashtest.csp.tm.service.TestCaseModificationService;

@NotThreadSafe
class AttachmentManagerServiceImplIT extends HibernateServiceSpecification {

	@Inject
	private TestCaseModificationService service

	@Inject
	private TestCaseLibraryNavigationService navService

	@Inject
	private TestCaseLibrariesCrudService libcrud

	@Inject
	private AttachmentManagerService attachService;




	private int testCaseId=-1;
	private int folderId = -1;
	private int attachListId = -1;


	def setup(){
		libcrud.addLibrary();

		def libList= libcrud.findAllLibraries()


		def lib = libList.get(libList.size()-1);

		def folder =  new TestCaseFolder(name:"folder")
		def testCase = new TestCase(name: "test case 1", description: "the first test case")

		navService.addFolderToLibrary(lib.id,folder)
		navService.addTestCaseToFolder (folder.id, testCase )

		folderId = folder.id;
		testCaseId= testCase.id;
		attachListId = testCase.attachmentList.id;
	}



	def "should create an AttachmentList along with a TestCase"(){
		given :


		when :
		def attachListId = service.findById(testCaseId).attachmentList.id;
		def attachList = service.findById(testCaseId).getAttachmentList();

		then :
		attachList != null
		attachList.id == attachListId;
	}


	def "should add a new attachment empty and retrieve it"(){
		given :

		Attachment attachment = new Attachment("attachment.doc");
		attachment.setType();

		when :
		Long id = attachService.addAttachment(attachListId, attachment)
		Attachment attach =  attachService.findAttachment(id);

		then :
		attach.name == "attachment.doc"
		attach.type == "doc"
	}

	def "should add and retrieve a lot of attachment headers"(){
		given :
		Attachment attachment = new Attachment("attachment.doc");

		Attachment attachment2 = new Attachment("attachment2.doc");

		Attachment attachment3 = new Attachment("attachment3.doc");
		when :
		List<Long> ids = []
		ids << attachService.addAttachment(attachListId, attachment)
		ids << attachService.addAttachment(attachListId, attachment2)
		ids << attachService.addAttachment(attachListId, attachment3)

		Set<Attachment> attached = attachService.findAttachments(attachListId)

		then :
		attached.collect{it.id}.containsAll (ids);
		attached.collect {it.name}.containsAll([
			"attachment.doc",
			"attachment2.doc",
			"attachment3.doc"
		])
		attached.collect {it.type}.containsAll(["doc", "doc", "doc"])
	}


	def "should create and add content to an attachment"(){
		given :
		Attachment attachment = new Attachment("attachment.doc");
		attachment.setType();

		byte[] bytes = new String("new long string just for the purpose of test").getBytes();

		when :
		Long id = attachService.addAttachment(attachListId, attachment)

		InputStream stream = new ByteArrayInputStream(bytes);

		attachService.setAttachmentContent(stream, id);


		then :
		notThrown(Exception)
	}


	byte[] randomBytes(int howMany){
		byte [] result = new byte[howMany];

		for (int i=0;i<howMany;i++){
			result[i]=Math.round(Math.random()*255);
		}

		return result;
	}

	def "should create an attachment, add content, retrieve the attachment and reread the content"(){

		given :
		Attachment attachment = new Attachment("attachment.doc");
		attachment.setType();

		byte[] bytes = randomBytes(100000)

		when :
		Long id = attachService.addAttachment(attachListId, attachment)

		InputStream stream = new ByteArrayInputStream(bytes);

		attachService.setAttachmentContent(stream, id);

		Attachment reattachment = attachService.findAttachment(id);

		InputStream restream = attachService.getAttachmentContent(id);

		byte[] res = new byte[100000]
		restream.read(res,0,100000)


		then :
		reattachment.name == "attachment.doc"
		reattachment.type == "doc"

		res == bytes
	}

	def "should create a complete attachment and persist it"(){
		given :
		Attachment attachment = new Attachment("attachment.doc");

		byte[] bytes = randomBytes(100000)

		when :
		AttachmentContent content = new AttachmentContent()
		InputStream stream = new ByteArrayInputStream(bytes);
		content.setContent(stream);

		attachment.setContent(content);

		Long id = attachService.addAttachment(attachListId, attachment)

		Attachment reattachment = attachService.findAttachment(id);

		InputStream restream = attachService.getAttachmentContent(id);

		byte[] res = new byte[100000]
		restream.read(res,0,100000)


		then :
		reattachment.name == "attachment.doc"
		reattachment.type == "doc"

		res == bytes
	}

	def "should remove an attachment"(){

		given :
		Attachment attachment = new Attachment("attachment.doc");
		attachment.setType();

		byte[] bytes = randomBytes(100000)

		AttachmentContent content = new AttachmentContent()
		InputStream stream = new ByteArrayInputStream(bytes);
		content.setContent(stream);

		attachment.setContent(content);

		Long id = attachService.addAttachment(attachListId, attachment)

		when :
		attachService.removeAttachmentFromList(attachListId, id)

		Set<Attachment> attached = attachService.findAttachments(attachListId)


		then :
		attached.size()==0
	}

	@Transactional
	def "should correctly tell if a test case have attachments or not"(){

		given :
		Attachment attachment = new Attachment("attachment.doc");
		attachment.setType();

		byte[] bytes = randomBytes(100000)

		AttachmentContent content = new AttachmentContent()
		InputStream stream = new ByteArrayInputStream(bytes);
		content.setContent(stream);

		attachment.setContent(content);

		when :
		// works because method marked transactional ! it should throw a lazy ex because fetch does not initialize attachments !
		TestCase testCase = service.findById(testCaseId);
		def shouldBeFalse = testCase.attachmentList.hasAttachments()

		attachService.addAttachment(attachListId, attachment);

		TestCase testCase2 = service.findById(testCaseId);
		def shouldBeTrue = testCase2.attachmentList.hasAttachments()

		then :
		shouldBeFalse == false;
		shouldBeTrue == true;
	}

}
