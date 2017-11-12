package com.jme3.gde.scenecomposer;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.renderer.Camera;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * In order to display the Camera Position and LookAt in Realtime,
 * we attach this AppState, so we have a callback on each frame
 * @author MeFisto94
 */
public class CameraPositionTrackerAppState extends BaseAppState {
    JLabel lblPos;
    JLabel lblLookAt;
    
    public CameraPositionTrackerAppState(JLabel lblPos, JLabel lblLookAt) {
        this.lblPos = lblPos;
        this.lblLookAt = lblLookAt;
    }

    @Override
    protected void initialize(Application aplctn) { }

    @Override
    protected void cleanup(Application aplctn) { }

    @Override
    protected void onEnable() { }

    @Override
    protected void onDisable() { }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        
        final Camera cam = getApplication().getCamera();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                lblPos.setText(cam.getLocation().toString());
                lblLookAt.setText(cam.getDirection().toString());
            }
        });
    }
}
