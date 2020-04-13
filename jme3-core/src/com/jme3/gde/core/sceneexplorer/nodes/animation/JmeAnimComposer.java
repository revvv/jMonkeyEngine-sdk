/*
 *  Copyright (c) 2009-2020 jMonkeyEngine
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.core.sceneexplorer.nodes.animation;

import com.jme3.anim.AnimComposer;
import com.jme3.gde.core.icons.IconList;
import com.jme3.gde.core.sceneexplorer.nodes.JmeControl;
import com.jme3.gde.core.sceneexplorer.nodes.JmeTrackChildren;
import com.jme3.gde.core.sceneexplorer.nodes.SceneExplorerNode;
import com.jme3.gde.core.sceneexplorer.nodes.actions.ControlsPopup;
import java.awt.Image;
import javax.swing.Action;
import org.openide.actions.DeleteAction;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.actions.SystemAction;

/**
 * Visual representation of the AnimComposer Class in the Scene Explorer
 * @author MeFisto94
 */
@org.openide.util.lookup.ServiceProvider(service = SceneExplorerNode.class)
@SuppressWarnings({"unchecked", "rawtypes"})
public class JmeAnimComposer extends JmeControl {
    private AnimComposer animComposer;
    private JmeAnimClip playingAnimation = null;
    private boolean displayBoneTracks = false;
    private boolean displayEffectTracks = true;
    private boolean displayAudioTracks = true;
    private static Image smallImage = IconList.animControl.getImage();

    public JmeAnimComposer() {
    }

    public JmeAnimComposer(AnimComposer animComposer, JmeAnimClipChildren children, DataObject obj) {
        super(children);
        dataObject = obj;
        children.setDataObject(dataObject);
        this.animComposer = animComposer;
        lookupContents.add(this);
        lookupContents.add(animComposer);
        setName("AnimComposer");
        children.setAnimComposer(this);
        control = animComposer;
    }

    @Override
    public Image getIcon(int type) {
        return smallImage;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return smallImage;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();
        set.setDisplayName("AnimComposer");
        set.setName(AnimComposer.class.getName());

        if (animComposer != null) {
            //set.put(new AnimationProperty(animComposer));
            sheet.put(set);
        } // else: Empty Sheet
        
        return sheet;
    }

    public boolean isPlaying() {
        return playingAnimation != null;
    }

    public void setAnimClip(JmeAnimClip anim) {
        if (playingAnimation != null) {
            playingAnimation.stop();
        }
        playingAnimation = anim;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
                    //new TrackVisibilityPopup(this),
                    new ControlsPopup(this),
                    SystemAction.get(DeleteAction.class)
                };
    }

    @Override
    public Class getExplorerObjectClass() {
        return AnimComposer.class;
    }

    @Override
    public Class getExplorerNodeClass() {
        return JmeAnimComposer.class;
    }

    @Override
    public Node[] createNodes(Object key, DataObject key2, boolean cookie) {
        JmeAnimClipChildren children = new JmeAnimClipChildren(this);
        return new Node[]{ new JmeAnimComposer((AnimComposer)key, children, key2)};
    }

    public boolean isDisplayAudioTracks() {
        return displayAudioTracks;
    }

    public boolean isDisplayBoneTracks() {
        return displayBoneTracks;
    }

    public boolean isDisplayEffectTracks() {
        return displayEffectTracks;
    }

    public void setDisplayAudioTracks(boolean displayAudioTracks) {
        this.displayAudioTracks = displayAudioTracks;
        refreshChildren();
    }

    public void setDisplayBoneTracks(boolean displayBoneTracks) {
        this.displayBoneTracks = displayBoneTracks;
        refreshChildren();
    }

    public void setDisplayEffectTracks(boolean displayEffectTracks) {
        this.displayEffectTracks = displayEffectTracks;
        refreshChildren();
    }

    public void refreshChildren() {
        ((JmeAnimChildren)this.jmeChildren).refreshChildren(true);
        for (Object node : getChildren().getNodes()) {
            JmeAnimation anim = (JmeAnimation) node;
            ((JmeTrackChildren) anim.getChildren()).refreshChildren(true);
        }
    }

    /*
    class ToggleBoneTrackAction extends BooleanStateAction {

        @Override
        public String getName() {
            return "Display bone tracks";
        }

        @Override
        public void setBooleanState(boolean value) {
            super.setBooleanState(value);
            displayBoneTracks = value;
            for (Object node : getChildren().getNodes()) {
                JmeAnimation anim = (JmeAnimation) node;
                ((JmeTrackChildren) anim.getChildren()).refreshChildren(true);
            }
        }

        @Override
        public boolean getBooleanState() {
            return displayBoneTracks;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return JmeAnimComposer.this.getHelpCtx();
        }
    };

    class ToggleEffectTrackAction extends BooleanStateAction {

        @Override
        public String getName() {
            return "Display effect tracks";
        }

        @Override
        public void setBooleanState(boolean value) {
            super.setBooleanState(value);
            displayEffectTracks = value;
            for (Object node : getChildren().getNodes()) {
                JmeAnimation anim = (JmeAnimation) node;
                ((JmeTrackChildren) anim.getChildren()).refreshChildren(true);
            }
        }

        @Override
        public boolean getBooleanState() {
            return displayEffectTracks;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return JmeAnimComposer.this.getHelpCtx();
        }
    };

    class ToggleAudioTrackAction extends BooleanStateAction {

        @Override
        public String getName() {
            return "Display audio tracks";
        }

        @Override
        public void setBooleanState(boolean value) {
            super.setBooleanState(value);
            displayAudioTracks = value;
            for (Object node : getChildren().getNodes()) {
                JmeAnimation anim = (JmeAnimation) node;
                ((JmeTrackChildren) anim.getChildren()).refreshChildren(true);
            }
        }

        @Override
        public boolean getBooleanState() {
            return displayAudioTracks;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return JmeAnimComposer.this.getHelpCtx();
        }
    };
    */
}
