package tigase.gwt.components.roster.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tigase.xmpp4gwt.client.JID;
import tigase.xmpp4gwt.client.stanzas.Presence;
import tigase.xmpp4gwt.client.xmpp.roster.RosterItem;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Roster extends Composite {

	public static interface GroupShowOfflineCallback {

		boolean isGroupShowsOffline(String groupName);

	}

	private final Map<JID, Set<Group>> buddies = new HashMap<JID, Set<Group>>();

	private ContactComparator contactComparator = new ContactComparator() {

		public int compare(Item o1, Item o2) {
			return o1.getName().compareTo(o2.getName());
		}

	};

	private String defaultGroupName = "General";

	private boolean defaultShowOfflineContacts = false;

	private GroupComparator groupComparator = new GroupComparator() {
		public int compare(Group o1, Group o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	private final Map<String, Group> groups = new HashMap<String, Group>();

	private GroupShowOfflineCallback groupShowOfflineCallback;

	private final List<RosterListener> listeners = new ArrayList<RosterListener>();

	private final VerticalPanel panel = new VerticalPanel();

	private PresenceCallback presenceCallback = new PresenceCallback() {
		public RosterPresence getRosterPresence(JID presence) {
			return RosterPresence.ERROR;
		}
	};

	private Group selectedGroup;

	private JID selectedJID;

	private Widget selectedPanel;

	private boolean showTransportAsContacts = false;

	public Roster(PresenceCallback presenceCallback) {
		initWidget(panel);
		setWidth("100%");
		this.groupShowOfflineCallback = new GroupShowOfflineCallback() {

			public boolean isGroupShowsOffline(String groupName) {
				return defaultShowOfflineContacts;
			}
		};
		this.presenceCallback = presenceCallback;
	}

	public void addAlwaysVisibleGroups(String... groupNames) {
		for (String groupName : groupNames) {
			Group group = this.groups.get(groupName);
			if (group == null) {
				group = new Group(this, groupName);
				this.groups.put(groupName, group);
				int index = 0;
				for (int i = 0; i < this.panel.getWidgetCount(); i++) {
					Widget w = this.panel.getWidget(i);
					if (w instanceof Group) {
						if (this.groupComparator.compare((Group) w, group) < 0) {
							index = i + 1;
						}
					}
				}
				panel.insert(group, index);
			}
			group.setVisibleIfEmpty(true);
			group.setStaticGroup(true);
		}
	}

	public void addListener(RosterListener listener) {
		this.listeners.add(listener);
	}

	void callContactContextMenu(Event event, Item item) {
		fireOnContactContextMenu(event, item);
	}

	void callContactDoubleClick(Event event, Item item) {
		fireOnContactDoubleClick(event, item);
	}

	void callGroupsContextMenu(Event event, Group group) {
		fireOnGroupContextMenu(event, group);
	}

	private void fireAfterRosterChange() {
		for (RosterListener listener : this.listeners) {
			listener.afterRosterChange();
		}
	}

	private void fireOnContactContextMenu(final Event event, final Item item) {
		for (RosterListener listener : this.listeners) {
			listener.onContactContextMenu(event, item);
		}
	}

	private void fireOnContactDoubleClick(final Event event, final Item item) {
		for (RosterListener listener : this.listeners) {
			listener.onContactDoubleClick(item);
		}
	}

	private void fireOnGroupContextMenu(final Event event, final Group group) {
		for (RosterListener listener : this.listeners) {
			listener.onGroupContextMenu(event, group);
		}
	}

	private void fireOnRosterItemSelect(final JID jid) {
		for (RosterListener listener : this.listeners) {
			listener.onRosterItemSelect(jid);
		}
	}

	public ContactComparator getContactComparator() {
		return contactComparator;
	}

	public boolean getGlobalShowOfflineContacts() {
		return defaultShowOfflineContacts;
	}

	public GroupComparator getGroupComparator() {
		return groupComparator;
	}

	public GroupShowOfflineCallback getGroupShowOfflineCallback() {
		return groupShowOfflineCallback;
	}

	public String[] getGroupsNames() {
		return this.groups.keySet().toArray(new String[] {});
	}

	PresenceCallback getPresenceCallback() {
		return presenceCallback;
	}

	public Group getSelectedGroup() {
		return selectedGroup;
	}

	public JID getSelectedJID() {
		return selectedJID;
	}

	public void removedFromRoster(RosterItem item) {
		JID jid = JID.fromString(item.getJid());
		Set<Group> buddyGruops = this.buddies.get(jid);
		if (buddyGruops != null) {
			for (Group group : buddyGruops) {
				group.remove(jid);

			}
		}

		Iterator<Group> git = buddyGruops.iterator();
		while (git.hasNext()) {
			Group g = git.next();
			if (!g.isStaticGroup() && g.getBuddiesCount() == 0) {
				this.panel.remove(g);
				this.groups.remove(g.getName());
				if (this.selectedGroup == g) {
					select(null, null);
				}
			}
		}
		fireAfterRosterChange();
	}

	public void removeListener(RosterListener listener) {
		this.listeners.remove(listener);
	}

	public void reset() {
		this.buddies.clear();
		this.groups.clear();
		this.panel.clear();
		this.selectedGroup = null;
		this.selectedJID = null;
		this.selectedPanel = null;
	}

	void select(Widget panel, Group group) {
		if (this.selectedPanel != null) {
			this.selectedPanel.removeStyleName("selected");
		}
		this.selectedJID = null;
		this.selectedGroup = group;
		this.selectedPanel = panel;
		if (this.selectedPanel != null)
			this.selectedPanel.addStyleName("selected");
	}

	void select(Widget panel, JID jid, Group group) {
		if (this.selectedPanel != null) {
			this.selectedPanel.removeStyleName("selected");
		}
		this.selectedPanel = panel;
		this.selectedJID = jid;
		this.selectedGroup = group;
		if (this.selectedPanel != null)
			this.selectedPanel.addStyleName("selected");
		fireOnRosterItemSelect(jid);
	}

	public void setContactComparator(ContactComparator contactComparator) {
		this.contactComparator = contactComparator;
	}

	public void setGlobalShowOfflineContacts(boolean value) {
		if (value != defaultShowOfflineContacts) {
			defaultShowOfflineContacts = value;
			for (Group group : this.groups.values()) {
				group.setShowOffline(value);
			}
		}
	}

	public void setGroupComparator(GroupComparator groupComparator) {
		this.groupComparator = groupComparator;
	}

	public void setGroupShowOfflineCallback(GroupShowOfflineCallback groupShowOfflineCallback) {
		this.groupShowOfflineCallback = groupShowOfflineCallback;
	}

	public void updatedRosterItem(RosterItem item) {
		// String[] groups = item.getGroups();
		if (!showTransportAsContacts && item.getJid() != null && JID.fromString(item.getJid()).getNode() == null)
			return;

		final RosterPresence rp = presenceCallback.getRosterPresence(JID.fromString(item.getJid()));

		List<String> groups = Arrays.asList(item.getGroups());
		if (groups.size() == 0) {
			groups = new ArrayList<String>();
			groups.add(this.defaultGroupName);
		}
		JID jid = JID.fromString(item.getJid());
		Set<Group> buddyGruops = this.buddies.get(jid);
		if (buddyGruops == null) {
			buddyGruops = new HashSet<Group>();
			this.buddies.put(jid, buddyGruops);
		}
		for (String groupName : groups) {
			Group group = this.groups.get(groupName);
			if (group == null) {
				group = new Group(this, groupName);
				group.setShowOffline(this.groupShowOfflineCallback.isGroupShowsOffline(groupName));
				this.groups.put(groupName, group);
				int index = 0;
				for (int i = 0; i < this.panel.getWidgetCount(); i++) {
					Widget w = this.panel.getWidget(i);
					if (w instanceof Group) {
						if (this.groupComparator.compare((Group) w, group) < 0) {
							index = i + 1;
						}
					}
				}
				panel.insert(group, index);
			}
			buddyGruops.add(group);
			group.updateRosterItem(jid, item);
			group.updatePresence(jid, rp);
		}
		Iterator<Group> git = buddyGruops.iterator();
		while (git.hasNext()) {
			Group g = git.next();
			if (!groups.contains(g.getName())) {
				git.remove();
				g.remove(jid);
			}
			if (!g.isStaticGroup() && g.getBuddiesCount() == 0) {
				this.panel.remove(g);
				this.groups.remove(g.getName());
				if (this.selectedGroup == g) {
					select(null, null);
				}
			}
		}

		fireAfterRosterChange();
	}

	public void updatePresence(Presence presenceItem) {
		if (presenceItem == null)
			return;
		if (!showTransportAsContacts && presenceItem.getFrom() != null && presenceItem.getFrom().getNode() == null)
			return;
		RosterPresence p = presenceCallback.getRosterPresence(presenceItem.getFrom());
		JID jid = presenceItem.getFrom().getBareJID();
		Set<Group> buddyGruops = this.buddies.get(jid);
		if (buddyGruops == null)
			return;
		for (Group group : buddyGruops) {
			group.updatePresence(jid, p);
		}
		fireAfterRosterChange();
	}

}
