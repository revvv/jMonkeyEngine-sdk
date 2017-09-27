/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.scenecomposer;

import com.jme3.asset.AssetManager;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.scene.controller.SceneToolController;
import com.jme3.gde.core.sceneexplorer.nodes.AbstractSceneExplorerNode;
import com.jme3.gde.core.sceneexplorer.nodes.JmeNode;
import com.jme3.gde.scenecomposer.gizmo.GizmoFactory;
import com.jme3.gde.scenecomposer.tools.shortcuts.ShortcutManager;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.math.Vector2f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import org.openide.util.Lookup;

/**
 *
 * @author Brent Owens
 */
public class SceneComposerToolController extends SceneToolController {

    private JmeNode rootNode;
    private SceneEditTool editTool;
    private SceneEditorController editorController;
    private ViewPort overlayView;
    private Node onTopToolsNode;
    private Node nonSpatialMarkersNode;
    private HashMap<AbstractSceneExplorerNode, Spatial> nonSpatialMarkers;

    private boolean snapToGrid = false;
    private boolean snapToScene = false;
    private boolean selectTerrain = false;
    private boolean selectGeometries = false;
    private TransformationType transformationType = TransformationType.local;

    public enum TransformationType {
        local, global, camera
    }

    public SceneComposerToolController(final Node toolsNode, AssetManager manager, JmeNode rootNode) {
        super(toolsNode, manager);
        this.rootNode = rootNode;
        nonSpatialMarkersNode = new Node("lightMarkersNode");
        nonSpatialMarkers = new HashMap<AbstractSceneExplorerNode, Spatial>();
        SceneApplication.getApplication().enqueue(new Callable<Object>() {

            public Object call() throws Exception {
                toolsNode.attachChild(nonSpatialMarkersNode);
                return null;
            }
        });
        setShowGrid(showGrid);
    }

    public SceneComposerToolController(AssetManager manager) {
        super(manager);
    }

    public void setEditorController(SceneEditorController editorController) {
        this.editorController = editorController;
    }

    public void createOnTopToolNode() {
        // a node in a viewport that will always render on top
        onTopToolsNode = new Node("OverlayNode");
        overlayView = SceneApplication.getApplication().getOverlayView();
        SceneApplication.getApplication().enqueue(new Callable<Void>() {

            public Void call() throws Exception {
                overlayView.attachScene(onTopToolsNode);
                return null;
            }
        });
    }

    @Override
    public void cleanup() {
        super.cleanup();
        editorController = null;
        SceneApplication.getApplication().enqueue(new Callable<Void>() {

            public Void call() throws Exception {
                overlayView.detachScene(onTopToolsNode);
                onTopToolsNode.detachAllChildren();
                return null;
            }
        });
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        if (editTool != null) {
            editTool.doUpdateToolsTransformation();
        }
        if (onTopToolsNode != null) {
            onTopToolsNode.updateLogicalState(tpf);
            onTopToolsNode.updateGeometricState();
        }
    }

    @Override
    public void render(RenderManager rm) {
        super.render(rm);
    }

    public boolean isEditToolEnabled() {
        return editTool != null;
    }

    /**
     * If the current tool overrides camera zoom/pan controls
     *
     * @return
     */
    public boolean isOverrideCameraControl() {
        if (editTool != null) {
            return editTool.isOverrideCameraControl();
        } else {
            return false;
        }
    }

    /**
     * Scene composer edit tool activated. Pass in null to remove tools.
     *
     * @param sceneEditTool pass in null to hide any existing tool markers
     */
    public void showEditTool(final SceneEditTool sceneEditTool) {
        SceneApplication.getApplication().enqueue(new Callable<Object>() {

            public Object call() throws Exception {
                doEnableEditTool(sceneEditTool);
                return null;
            }
        });
    }

    private void doEnableEditTool(SceneEditTool sceneEditTool) {
        if (editTool != null) {
            editTool.hideMarker();
        }
        editTool = sceneEditTool;
        editTool.activate(manager, toolsNode, onTopToolsNode, selected, this);
    }

    public void selectedSpatialTransformed() {
        if (editTool != null) {
            editTool.updateToolsTransformation();
        }
    }

    public void setSelected(Spatial selected) {
        this.selected = selected;
    }

    public void setNeedsSave(boolean needsSave) {
        editorController.setNeedsSave(needsSave);
    }

    /**
     * Primary button activated, send command to the tool for appropriate
     * action.
     *
     * @param mouseLoc
     * @param pressed
     * @param camera
     */
    public void doEditToolActivatedPrimary(Vector2f mouseLoc, boolean pressed, Camera camera) {
        ShortcutManager scm = Lookup.getDefault().lookup(ShortcutManager.class);

        if (scm.isActive()) {
            scm.getActiveShortcut().setCamera(camera);
            scm.getActiveShortcut().actionPrimary(mouseLoc, pressed, rootNode, editorController.getCurrentDataObject());
        } else if (editTool != null) {
            editTool.setCamera(camera);
            editTool.actionPrimary(mouseLoc, pressed, rootNode, editorController.getCurrentDataObject());
        }
    }

    /**
     * Secondary button activated, send command to the tool for appropriate
     * action.
     *
     * @param mouseLoc
     * @param pressed
     * @param camera
     */
    public void doEditToolActivatedSecondary(Vector2f mouseLoc, boolean pressed, Camera camera) {
        ShortcutManager scm = Lookup.getDefault().lookup(ShortcutManager.class);

        if (scm.isActive()) {
            scm.getActiveShortcut().setCamera(camera);
            scm.getActiveShortcut().actionSecondary(mouseLoc, pressed, rootNode, editorController.getCurrentDataObject());
        } else if (editTool != null) {
            editTool.setCamera(camera);
            editTool.actionSecondary(mouseLoc, pressed, rootNode, editorController.getCurrentDataObject());
        }
    }

    public void doEditToolMoved(Vector2f mouseLoc, Camera camera) {
        ShortcutManager scm = Lookup.getDefault().lookup(ShortcutManager.class);

        if (scm.isActive()) {
            scm.getActiveShortcut().setCamera(camera);
            scm.getActiveShortcut().mouseMoved(mouseLoc, rootNode, editorController.getCurrentDataObject());
        } else if (editTool != null && editorController != null) {
            editTool.setCamera(camera);
            editTool.mouseMoved(mouseLoc, rootNode, editorController.getCurrentDataObject());
        }
    }

    public void doEditToolDraggedPrimary(Vector2f mouseLoc, boolean pressed, Camera camera) {
        ShortcutManager scm = Lookup.getDefault().lookup(ShortcutManager.class);

        if (scm.isActive()) {
            scm.getActiveShortcut().setCamera(camera);
            scm.getActiveShortcut().draggedPrimary(mouseLoc, pressed, rootNode, editorController.getCurrentDataObject());
        } else if (editTool != null) {
            editTool.setCamera(camera);
            editTool.draggedPrimary(mouseLoc, pressed, rootNode, editorController.getCurrentDataObject());
        }
    }

    public void doEditToolDraggedSecondary(Vector2f mouseLoc, boolean pressed, Camera camera) {
        ShortcutManager scm = Lookup.getDefault().lookup(ShortcutManager.class);

        if (scm.isActive()) {
            scm.getActiveShortcut().setCamera(null);
            scm.getActiveShortcut().draggedSecondary(mouseLoc, pressed, rootNode, editorController.getCurrentDataObject());
        } else if (editTool != null) {
            editTool.setCamera(camera);
            editTool.draggedSecondary(mouseLoc, pressed, rootNode, editorController.getCurrentDataObject());
        }
    }

    public void doKeyPressed(KeyInputEvent kie) {
        ShortcutManager scm = Lookup.getDefault().lookup(ShortcutManager.class);

        if (scm.isActive()) {
            scm.doKeyPressed(kie);
        } else if (scm.activateShortcut(kie)) {
            scm.getActiveShortcut().activate(manager, toolsNode, onTopToolsNode, selected, this);
        } else if (editTool != null) {
            editTool.keyPressed(kie);
        }
    }

    protected void refreshNonSpatialMarkers() {
        addMarkers();
    }

    private void getNodes(org.openide.nodes.Node node, List<AbstractSceneExplorerNode> list) {
        if (node instanceof AbstractSceneExplorerNode) {
            list.add((AbstractSceneExplorerNode) node);
        }
        if (!node.isLeaf()) {
            for (org.openide.nodes.Node n : node.getChildren().getNodes(true)) {
                getNodes(n, list);
            }
        }
    }

    private void addMarkers() {
        final List<AbstractSceneExplorerNode> nodes = new ArrayList<AbstractSceneExplorerNode>();
        // gather nodes, have to be in an other thread than the sceneApplication
        getNodes(rootNode, nodes);
        
        // then update markers
        SceneApplication.getApplication().enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                
                Iterator<AbstractSceneExplorerNode> it = nonSpatialMarkers.keySet().iterator();
                while (it.hasNext()) {
                    AbstractSceneExplorerNode n = it.next();
                    if (nodes.contains(n)) {
                        // a node is already added
                        nodes.remove(n);
                    } else {
                        // a node is no more needed
                        nonSpatialMarkers.get(n).removeFromParent();
                        it.remove();
                        //nonSpatialMarkers.remove(n);
                    }
                }

                it = nodes.iterator();
                while (it.hasNext()) {
                    AbstractSceneExplorerNode n = it.next();
                    if (!nonSpatialMarkers.containsKey(n)) {
                        Spatial s = GizmoFactory.createGizmo(manager, n);
                        if (s != null) {
                            nonSpatialMarkers.put(n, s);
                            nonSpatialMarkersNode.attachChild(s);
                        }
                    }
                }
                return null;
            }
        });
    }

    public Spatial getMarker(AbstractSceneExplorerNode node) {
        return nonSpatialMarkers.get(node);
    }

    public boolean isSnapToGrid() {
        return snapToGrid;
    }

    public void setSnapToGrid(boolean snapToGrid) {
        this.snapToGrid = snapToGrid;
    }

    public void setSnapToScene(boolean snapToScene) {
        this.snapToScene = snapToScene;
    }

    public boolean isSnapToScene() {
        return snapToScene;
    }

    public boolean isSelectTerrain() {
        return selectTerrain;
    }

    public void setSelectTerrain(boolean selectTerrain) {
        this.selectTerrain = selectTerrain;
    }

    public boolean isSelectGeometries() {
        return selectGeometries;
    }

    public void setSelectGeometries(boolean selectGeometries) {
        this.selectGeometries = selectGeometries;
    }

    public void setTransformationType(String type) {
        if (type != null) {
            if (type.equals("Local")) {
                setTransformationType(TransformationType.local);
            } else if (type.equals("Global")) {
                setTransformationType(TransformationType.global);
            } else if (type.equals("Camera")) {
                setTransformationType(TransformationType.camera);
            }
        }
    }

    /**
     * @param type the transformationType to set
     */
    public void setTransformationType(TransformationType type) {
        if (type != this.transformationType) {
            this.transformationType = type;
            if (editTool != null) {
                //update the transform type of the tool
                editTool.setTransformType(transformationType);
            }
        }
    }

    /**
     * @return the transformationType
     */
    public TransformationType getTransformationType() {
        return transformationType;
    }
     
    public JmeNode getRootNode() {
        return rootNode;
    }

}
