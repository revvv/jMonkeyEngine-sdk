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

import com.jme3.anim.Armature;
import com.jme3.gde.core.icons.IconList;
import com.jme3.gde.core.sceneexplorer.nodes.AbstractSceneExplorerNode;
import com.jme3.gde.core.sceneexplorer.nodes.SceneExplorerNode;
import java.awt.Image;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;

/**
 * Visual representation of the Armature Class in the Scene Explorer
 * @author MeFisto94
 */
@org.openide.util.lookup.ServiceProvider(service = SceneExplorerNode.class)
@SuppressWarnings({"unchecked", "rawtypes"})
public class JmeArmature extends AbstractSceneExplorerNode {
    private Armature armature;
    private static Image smallImage = IconList.skeletonControl.getImage();

    public JmeArmature() {
    }

    public JmeArmature(Armature armature, JmeBoneChildren children) {
        super(children);
        this.armature = armature;
        lookupContents.add(this);
        lookupContents.add(armature);
        setName("Armature");
        //children.setSkeltonControl(this);
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
        set.setDisplayName("Armature");
        set.setName(Armature.class.getName());
        if (armature == null) {
            return sheet;
        }
        
        sheet.put(set);
        return sheet;
    }

    @Override
    public Class getExplorerObjectClass() {
        return Armature.class;
    }

    @Override
    public Class getExplorerNodeClass() {
        return JmeArmature.class;
    }

    public Armature getArmature() {
        return armature;
    }

    @Override
    public Node[] createNodes(Object key, DataObject key2, boolean cookie) {
        JmeBoneChildren children = new JmeBoneChildren(null, null);
        JmeArmature jsc = new JmeArmature((Armature) key, children);
        return new Node[]{jsc};
    }
}
