package org.squashtest.tm.domain.requirement;

import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.synchronisation.RemoteSynchronisation;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name = "REQUIREMENT_FOLDER_SYNC_EXTENDER")
public class RequirementFolderSyncExtender {

	@Id
	@Column(name = "REQUIREMENT_FOLDER_SYNC_EXTENDER_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "requirement_folder_sync_extender_requirement_folder_sync_extender_id")
	@SequenceGenerator(name = "requirement_folder_sync_extender_requirement_folder_sync_extender_id", sequenceName = "requirement_folder_sync_extender_requirement_folder_sync_extender_id", allocationSize = 1)
	private Long id;

	@Column(name = "REMOTE_FOLDER_ID")
	@Size(min = 0, max = Sizes.NAME_MAX)
	private String remoteFolderId;

	@Column(name = "REMOTE_FOLDER_STATUS")
	@Size(min = 0, max = Sizes.STATUS_MAX)
	private String remoteFolderStatus;

	@Column(name= "TYPE")
	@Enumerated(EnumType.STRING)
	private RequirementFolderSyncExtenderType type;

	@ManyToOne
	@JoinColumn(name = "REMOTE_SYNCHRONISATION_ID")
	private RemoteSynchronisation remoteSynchronisation;

	@OneToOne
	@JoinColumn(name = "REQUIREMENT_FOLDER_ID")
	private RequirementFolder requirementFolder;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRemoteFolderId() {
		return remoteFolderId;
	}

	public void setRemoteFolderId(String remoteFolderId) {
		this.remoteFolderId = remoteFolderId;
	}

	public String getRemoteFolderStatus() {
		return remoteFolderStatus;
	}

	public void setRemoteFolderStatus(String remoteFolderStatus) {
		this.remoteFolderStatus = remoteFolderStatus;
	}

	public RemoteSynchronisation getRemoteSynchronisation() {
		return remoteSynchronisation;
	}

	public void setRemoteSynchronisation(RemoteSynchronisation remoteSynchronisation) {
		this.remoteSynchronisation = remoteSynchronisation;
	}

	public RequirementFolder getRequirementFolder() {
		return requirementFolder;
	}

	public void setRequirementFolder(RequirementFolder requirementFolder) {
		this.requirementFolder = requirementFolder;
	}

	public RequirementFolderSyncExtenderType getType() {
		return type;
	}

	public void setType(RequirementFolderSyncExtenderType type) {
		this.type = type;
	}
}
