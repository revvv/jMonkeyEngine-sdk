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

import com.jme3.anim.AnimClip;
import com.jme3.anim.AnimComposer;
import com.jme3.animation.AnimChannel;
import com.jme3.gde.core.icons.IconList;
import com.jme3.gde.core.properties.AnimationProperty;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.sceneexplorer.nodes.AbstractSceneExplorerNode;
import com.jme3.gde.core.sceneexplorer.nodes.JmeTrackChildren;
import com.jme3.gde.core.sceneexplorer.nodes.SceneExplorerNode;
import com.jme3.gde.core.sceneexplorer.nodes.animation.JmeAnimation.PlayBackParamsAction;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.swing.Action;
import org.openide.actions.DeleteAction;
import org.openide.actions.RenameAction;
import org.openide.awt.Actions;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.actions.SystemAction;

/**
 * Visual representation of the AnimClip Class in the Scene Explorer
 * @author MeFisto94
 */
@org.openide.util.lookup.ServiceProvider(service = SceneExplorerNode.class)
@SuppressWarnings({"unchecked", "rawtypes"})
public class JmeAnimClip extends AbstractSceneExplorerNode {

    private AnimClip animClip;
    private Image icon;
    private JmeAnimComposer jmeControl;
    private boolean playing = false;
    private float animSpeed = 1.0f;
    private AnimChannel channel = null;

    public JmeAnimClip() {
    }

    public JmeAnimClip(JmeAnimComposer control, AnimClip animation, DataObject dataObject) {
        super();
        this.dataObject = dataObject;
        this.animClip = animation;
        this.jmeControl = control;
        lookupContents.add(this);
        lookupContents.add(animation);
        setName(animation.getName());
        icon = IconList.animation.getImage();

        addNodeListener(new NodeAdapter(){
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equalsIgnoreCase("name")){
                    doRenameAnimation((String) evt.getOldValue(),(String)evt.getNewValue());
                }
            }        
        });
    }
    
    @Override
    public Image getIcon(int type) {
        return icon;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return icon;
    }

    public void toggleIcon(boolean enabled) {
        if (!playing) {
            icon = IconList.animation.getImage();

        } else {
            icon = IconList.animationPlay.getImage();

        }
        fireIconChange();
    }

    @Override
    public Action getPreferredAction() {
        return Actions.alwaysEnabled(new PlayAction(), "Play", "", false);
    }

    private void play() {
        playing = !playing;
        toggleIcon(playing);
        jmeControl.setAnimClip(this);
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();
        set.setDisplayName("AnimClip");
        set.setName(AnimClip.class.getName());
        if (animClip != null) {
            //set.put(new AnimationProperty(jmeControl.getLookup().lookup(AnimComposer.class)));
            sheet.put(set);
        } // else: Empty Sheet
        
        return sheet;
    }

    public void setChanged() {
        fireSave(true);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{Actions.alwaysEnabled(new PlayAction(), playing ? "Stop" : "Play", "", false),
                    //Actions.alwaysEnabled(new PlayBackParamsAction(), "Playback parameters", "", false),
                    SystemAction.get(RenameAction.class),
                    SystemAction.get(DeleteAction.class),
                    //Actions.alwaysEnabled(new EffectTrackWizardAction(jmeControl.getLookup().lookup(AnimComposer.class).getSpatial(), this), "Add Effect Track", "", false),
                    //Actions.alwaysEnabled(new AudioTrackWizardAction(jmeControl.getLookup().lookup(AnimComposer.class).getSpatial(), this), "Add Audio Track", "", false),
                    // @TODO: not working yet, Actions.alwaysEnabled(new ExtractAnimationAction(), "Extract sub-animation", "", true)
                };
    }

    @Override
    public boolean canDestroy() {
         return !jmeControl.isReadOnly();
    }

    public void stop() {
        playing = false;
        toggleIcon(playing);
    }

    @Override
    public void destroy() throws IOException {
        super.destroy();     
        final AnimComposer control = jmeControl.getLookup().lookup(AnimComposer.class);
        try {
            lookupContents.remove(this.animClip);
            lookupContents.remove(this);
            SceneApplication.getApplication().enqueue(() -> {
                control.removeAnimClip(this.animClip);
                return null;
            }).get();            
            jmeControl.refreshChildren();
            setChanged();
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void refresh(boolean immediate) {
        super.refresh(immediate);
        ((JmeTrackChildren) getChildren()).refreshChildren(false);
    }

    @Override
    public Class getExplorerObjectClass() {
        return AnimClip.class;
    }

    @Override
    public Class getExplorerNodeClass() {
        return JmeAnimClip.class;
    }

    @Override
    public Node[] createNodes(Object key, DataObject key2, boolean cookie) {
        JmeAnimClip jsc = new JmeAnimClip(jmeControl, (AnimClip)key, key2);
        return new Node[]{jsc};
    }

    class PlayAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            final AnimComposer control = jmeControl.getLookup().lookup(AnimComposer.class);
            if (control == null) {
                return;
            }

            try {
                SceneApplication.getApplication().enqueue(() -> {
                    if (playing) { // Stop Playing
                        control.setCurrentAction(null);
                        jmeControl.setAnimClip(null);
                        return null;
                    } else {
                        control.setCurrentAction(animClip.getName());
                        control.setGlobalSpeed(animSpeed);
                        java.awt.EventQueue.invokeLater(() -> {
                            play();
                        });
                        return null;
                    }
                }).get();
            } catch (InterruptedException | ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /*class PlayBackParamsAction implements ActionListener {
        ChannelDialog dialog = new ChannelDialog(null, false, JmeAnimClip.this);

        @Override
        public void actionPerformed(ActionEvent e) {
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

        }
    }*/

    public float getAnimSpeed() {
        return animSpeed;
    }

    public void setAnimSpeed(float speed) {
        this.animSpeed = speed;
        try {
            SceneApplication.getApplication().enqueue(() -> {
                final AnimComposer composer = jmeControl.getLookup().lookup(AnimComposer.class);
                if (composer != null) {
                    composer.setGlobalSpeed(animSpeed);
                }
                return null;
            }).get();
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public boolean canRename() {
        return !jmeControl.isReadOnly();        
    }
    
    /*class ExtractAnimationAction implements ActionListener {
        ExtractSubAnimationDialog dialog = new ExtractSubAnimationDialog();

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                dialog.setAnimControl(JmeAnimClip.this.jmeControl);
                dialog.setAnimation(JmeAnimClip.this.animClip);
                //animation
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);

                JmeAnimClip.this.jmeControl.fireSave(true);
                JmeAnimClip.this.jmeControl.refresh(true);
                JmeAnimClip.this.jmeControl.refreshChildren();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }*/
    
    /**
     * Renames the animation in the OpenGL thread.
     * Note that renaming an animation mean to delete the old one and create a
     * new anim with the new name and the old data
     * @param evt 
     */
    protected void doRenameAnimation(final String oldName,final String newName) {
        final AnimComposer control = jmeControl.getLookup().lookup(AnimComposer.class);
        try {
            lookupContents.remove(JmeAnimClip.this.animClip);
            JmeAnimClip.this.animClip = SceneApplication.getApplication().enqueue(() -> {
                AnimClip anim = control.getAnimClip(oldName);
                AnimClip newAnim = new AnimClip(newName);
                newAnim.setTracks(anim.getTracks());
                control.removeAnimClip(anim);
                control.addAnimClip(newAnim);
                return newAnim;
            }).get();
            lookupContents.add(JmeAnimClip.this.animClip);
            setChanged();
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
