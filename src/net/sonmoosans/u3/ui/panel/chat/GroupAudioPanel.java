package net.sonmoosans.u3.ui.panel.chat;

import io.socket.client.Socket;
import net.sonmoosans.u3.api.AccountAPI;
import net.sonmoosans.u3.api.GroupAPI;
import net.sonmoosans.u3.api.model.Group;
import net.sonmoosans.u3.ui.AddableComponent;
import net.sonmoosans.u3.ui.AddablePanel;
import net.sonmoosans.u3.ui.layout.WrapLayout;
import net.sonmoosans.u3.ui.util.Key;
import net.sonmoosans.u3.ui.util.HashLinkedContainer;

import javax.sound.sampled.*;
import javax.swing.*;

import static net.sonmoosans.u3.ui.util.CommonUtil.*;

public class GroupAudioPanel extends AddablePanel {
    private JPanel Main;
    private JButton joinButton;
    private JCheckBox speakCheckBox;
    private JPanel memberContainer;
    private JButton leaveButton;
    private JPanel joinedPane;
    private boolean joined = false;
    private int groupID, joinedGroupID;

    private final HashLinkedContainer<UserKey, MemberItem, Integer> members = new HashLinkedContainer<>(
            memberContainer,
            key-> new MemberItem(key.value)
    );

    private static final AudioFormat format = getFormat();
    private static final DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, format);

    public GroupAudioPanel() {
        memberContainer.setLayout(new WrapLayout(WrapLayout.LEFT));

        memberContainer.setBackground(memberContainer.getBackground().darker());

        joinButton.addActionListener(e-> {
            if (!joined)
                joinCall(groupID);
        });

        leaveButton.addActionListener(e-> {
            if (joined)
                leaveCall();
        });

        speakCheckBox.addItemListener(e-> {
            if (speakCheckBox.isSelected())
                openMicrophone();
        });
    }

    public void initSocket(Socket socket) {
        socket.on(GroupAPI.JOIN_VOICE_EVENT, args -> {
            int groupID = (int) args[0];

            if (groupID == this.groupID) {
                addMember((int) args[1]);
            }
        });

        socket.on(GroupAPI.LEAVE_VOICE_EVENT, args -> {
            int groupID = (int) args[0];

            if (groupID == this.groupID) {
                members.remove((int) args[1]);
            }
        });

        try {
            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);

            speaker.open(format);
            speaker.start();

            socket.on(GroupAPI.VOICE_EVENT, args -> {
                byte[] data = (byte[]) args[0];
                speaker.write(data,0, (int) args[1]);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setGroup(Group group) {
        if (group.id == null) return;
        groupID = group.id;

        members.clear();

        runAsync(()-> GroupAPI.getVoiceCallMembers(group.id), result -> {
            if (result.isSuccess())
                for (int userID : result.context()) {
                    addMember(userID);
                }
        });

        if (joinedGroupID == groupID && joined) {
            joinButton.setVisible(false);
            joinedPane.setVisible(true);
        } else {
            joinButton.setVisible(true);
            joinButton.setEnabled(!joined);
            joinedPane.setVisible(false);
        }
    }

    private void addMember(int userID) {
        members.add(new UserKey(userID));
    }

    protected void joinCall(int groupID) {
        joinedPane.setVisible(true);
        joinButton.setVisible(false);
        GroupAPI.joinVoiceRoom(groupID);
        joinedGroupID = groupID;
        joined = true;
    }

    private void openMicrophone() {
        if (!joined) return;

        runAsync(()-> {
            try (TargetDataLine mic = (TargetDataLine) AudioSystem.getLine(micInfo)) {
                mic.open(format);
                mic.start();

                byte[] tmpBuff = new byte[mic.getBufferSize() / 8];

                while (speakCheckBox.isSelected() && joined) {
                    int count = mic.read(tmpBuff, 0, tmpBuff.length);

                    if (count > 0) {
                        GroupAPI.sendAudio(joinedGroupID, tmpBuff, count);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    protected void leaveCall() {
        joined = false;

        GroupAPI.leaveVoiceRoom(joinedGroupID);

        joinedPane.setVisible(false);
        joinButton.setVisible(true);
        joinButton.setEnabled(true);
    }

    private static AudioFormat getFormat() {
        float rate = 16000.0F;
        int channels = 1;
        int sampleSize = 16;

        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                rate,
                sampleSize,
                channels,
                (sampleSize / 8) * channels,
                rate,
                false
        );
    }

    @Override
    public JPanel getPanel() {
        return Main;
    }

    private static class MemberItem extends AddableComponent {
        private final JLabel iconHolder = new JLabel();

        public MemberItem(int userID) {
            runAsync(()-> getImage(AccountAPI.getUserSafe(userID).avatar), image ->
                    setIcon(iconHolder, image, 30)
            );
        }

        @Override
        public JComponent getComponent() {
            return iconHolder;
        }
    }

    private static record UserKey(int value) implements Key<Integer> {
        @Override
        public Integer getKey() {
            return value;
        }
    }
}
