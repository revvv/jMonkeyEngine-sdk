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
import com.jme3.gde.core.scene.SceneApplication;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 * Representation of multiple Animations in the Scene Explorer
 * @author MeFisto94
 */
public class JmeAnimClipChildren extends Children.Keys<Object> {
    protected JmeAnimComposer jmeAnimComposer;
    protected boolean readOnly = true;
    protected HashMap<Object, Node> map = new HashMap<>();
    private DataObject dataObject;

    public JmeAnimClipChildren() {
    }

    public JmeAnimClipChildren(JmeAnimComposer jmeAnimComposer) {
        this.jmeAnimComposer = jmeAnimComposer;
    }

    public void refreshChildren(boolean immediate) {
        setKeys(createKeys());
        refresh();
    }

    public void setReadOnly(boolean cookie) {
        this.readOnly = cookie;
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        setKeys(createKeys());
    }

    protected List<Object> createKeys() {
        try {
            return SceneApplication.getApplication().enqueue(() -> {
                List<Object> keys = new LinkedList<>();
                AnimComposer composer = jmeAnimComposer.getLookup().lookup(AnimComposer.class);
                if (composer != null) {
                    keys.addAll(composer.getAnimClips());
                }
                
                return keys;
            }).get();
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    protected Node[] createNodes(Object key) {
        if (key instanceof AnimClip) {
            return new Node[]{ new JmeAnimClip(jmeAnimComposer, (AnimClip)key, dataObject).setReadOnly(readOnly)};
        } else {
            return new Node[]{ Node.EMPTY };
        }
    }

    public void setAnimComposer(JmeAnimComposer jmeAnimComposer) {
        this.jmeAnimComposer = jmeAnimComposer;
    }

    public DataObject getDataObject() {
        return dataObject;
    }

    public void setDataObject(DataObject dataObject) {
        this.dataObject = dataObject;
    }
}
