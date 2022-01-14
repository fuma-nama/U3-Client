package net.sonmoosans.u3.api.model;

import net.sonmoosans.u3.ui.util.Key;

import java.util.Objects;

public class GroupInvite implements Key<GroupInvite> {
    public int groupID, senderID;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupInvite that = (GroupInvite) o;
        return groupID == that.groupID && senderID == that.senderID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupID, senderID);
    }

    @Override
    public GroupInvite getKey() {
        return this;
    }
}

