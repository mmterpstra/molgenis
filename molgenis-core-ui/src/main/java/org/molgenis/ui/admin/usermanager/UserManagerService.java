package org.molgenis.ui.admin.usermanager;

import org.molgenis.auth.MolgenisGroup;

import java.util.List;

public interface UserManagerService
{
	List<MolgenisUserViewData> getAllMolgenisUsers();

	void setActivationUser(String userId, Boolean active);

	void setActivationGroup(String groupId, Boolean active);

	List<MolgenisGroup> getAllMolgenisGroups();

	List<MolgenisGroup> getGroupsWhereUserIsMember(String userId);

	List<MolgenisGroup> getGroupsWhereUserIsNotMember(String userId);

	List<MolgenisUserViewData> getUsersMemberInGroup(String groupId);

	void addUserToGroup(String molgenisGroupId, String molgenisUserId);

	void removeUserFromGroup(String molgenisGroupId, String molgenisUserId);
}
