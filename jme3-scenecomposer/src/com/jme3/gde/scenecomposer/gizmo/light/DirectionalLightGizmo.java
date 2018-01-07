/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.scenecomposer.gizmo.light;

import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.gde.core.sceneexplorer.nodes.JmeDirectionalLight;
import com.jme3.gde.core.sceneexplorer.nodes.gizmo.DirectionalLightGizmoInterface;
import com.jme3.gde.scenecomposer.SceneComposerToolController;
import com.jme3.gde.scenecomposer.SceneComposerTopComponent;
import com.jme3.gde.scenecomposer.SceneEditTool;
import com.jme3.gde.scenecomposer.gizmo.NodeCallback;
import com.jme3.light.DirectionalLight;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 * Handles the DirectionalLight's Gizmo.
 * 
 * Note: There are actually three classes involved:
 * The DirectionalLight (jme class) is not added to the gizmo.
 * The Gizmo is a seperate node, which just passes it's transforms to
 * the lights transform (powered by NodeCallback) (but the light obviously has
 * another parent, because that affects lighting).
 * 
 * The JmeDirectionalLight / JmeLight is responsible for making the Light appear
 * in SceneExplorer as node and it handles the properties.
 * 
 * @author dokthar
 */
public class DirectionalLightGizmo extends NodeCallback implements DirectionalLightGizmoInterface {
    private final Vector3f initialDirection;
    private final JmeDirectionalLight jmeLight;
    private final DirectionalLight light;

    public DirectionalLightGizmo(JmeDirectionalLight jmelight) {
        super("directional light gizmo", true, true, false);
        jmeLight = jmelight;
        light = jmeLight.getLookup().lookup(DirectionalLight.class);
        initialDirection = light.getDirection().clone();
    }

    @Override
    public void onTranslation(Vector3f oldTranslation, Vector3f newTranslation) {
    }

    @Override
    public void onResize(Vector3f oldScale, Vector3f newScale) {
    }

    @Override
    public void onRotation(Quaternion oldRotation, Quaternion newRotation) {
        light.setDirection(newRotation.mult(initialDirection));
        jmeLight.setValue("direction", light.getDirection());
    }
    
    @Override
    public void onSetDirection(Vector3f direction) {
        // We cannot set the light direction directly, we have to adjust the
        // rotation as well (which in turn calls onRotation to set the direction)
         
        // Black Quaternion Magic taken from https://stackoverflow.com/a/11741520
        float k_cos_theta = initialDirection.dot(direction);
        float k = FastMath.sqrt(initialDirection.lengthSquared() * direction.lengthSquared());

        if (FastMath.approximateEquals(k_cos_theta / k, 1)) {
            setLocalRotation(Quaternion.IDENTITY);
        } else if (FastMath.approximateEquals(k_cos_theta / k, -1f)) {
            // 180 degree rotation around any orthogonal vector
            Vector3f anyOrthogonal = initialDirection.cross(Vector3f.UNIT_Y).normalize();
            setLocalRotation(new Quaternion(anyOrthogonal.x, anyOrthogonal.y, anyOrthogonal.z, 0));
        } else {
            Vector3f xyz = initialDirection.cross(direction);
            setLocalRotation(new Quaternion(xyz.x, xyz.y, xyz.z, k_cos_theta + k));
        }
        
        SceneComposerToolController sctc = SceneComposerTopComponent.findInstance().getToolController();
        if (sctc.getSelectedSpatial() != null) {
           sctc.selectedSpatialTransformed();
        }
    }

    private final BoundingSphere bv = new BoundingSphere(10f, getWorldTranslation());

    @Override
    public BoundingVolume getWorldBound() {
        return bv;
    }

}
