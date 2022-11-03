package com.example.ardemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.view.isGone
import androidx.core.view.isVisible
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.utils.setFullScreen

// Ar model viewer demo
// https://github.com/SceneView/sceneview-android#ar-model-viewer

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    data class Model(
        val fileLocation: String,
        val scaleUnits: Float? = null,
        val placementMode: PlacementMode = PlacementMode.BEST_AVAILABLE,
        val applyPoseRotation: Boolean = true
    )

    private lateinit var sceneView: ArSceneView
    private lateinit var loadingView: View
    private lateinit var placeModelButton: Button

    private var modelNode: ArModelNode? = null

    private val models = listOf(
        Model("models/spiderbot.glb"),
        Model(
            fileLocation = "https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb",
            // Display the Tiger with a size of 3 m long
            scaleUnits = 2.5f,
            placementMode = PlacementMode.BEST_AVAILABLE,
            applyPoseRotation = false
        ),
        Model(
            fileLocation = "https://sceneview.github.io/assets/models/DamagedHelmet.glb",
            placementMode = PlacementMode.INSTANT,
            scaleUnits = 0.5f
        ),
        Model(
            fileLocation = "https://storage.googleapis.com/ar-answers-in-search-models/static/GiantPanda/model.glb",
            placementMode = PlacementMode.PLANE_HORIZONTAL,
            // Display the Tiger with a size of 1.5 m height
            scaleUnits = 1.5f
        ),
        Model(
            fileLocation = "https://sceneview.github.io/assets/models/Spoons.glb",
            placementMode = PlacementMode.PLANE_HORIZONTAL_AND_VERTICAL,
            // Keep original model size
            scaleUnits = null
        ),
        Model(
            fileLocation = "https://sceneview.github.io/assets/models/Halloween.glb",
            placementMode = PlacementMode.PLANE_HORIZONTAL,
            scaleUnits = 2.5f
        ),
    )


//    private val model = models[4]
//    private val model = models[1]
    private val model = models[0]

    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        newModelNode()
    }

    private fun initViews() {
        setFullScreen(
            findViewById(R.id.rootView),
            fullScreen = true,
            hideSystemBars = true,
            fitsSystemWindows = false
        )
        sceneView = findViewById(R.id.sceneView)
        loadingView = findViewById(R.id.loadingView)
        placeModelButton = findViewById<Button>(R.id.placeModelButton).apply {
            setOnClickListener { placeModelNode() }
        }
    }

    private fun newModelNode() {
        isLoading = true
//        modelNode = ArModelNode(
//            placementMode = PlacementMode.BEST_AVAILABLE,
//            hitPosition = Position(0.0f, 0.0f, -2.0f),
//            followHitPosition = true,
//            instantAnchor = false
//        )
        modelNode = ArModelNode(this@MainActivity.model.placementMode).apply {
            applyPoseRotation = this@MainActivity.model.applyPoseRotation
            loadModelAsync(
                context = this@MainActivity,
                lifecycle = lifecycle,
                glbFileLocation = this@MainActivity.model.fileLocation,
                autoAnimate = true,
                scaleToUnits = this@MainActivity.model.scaleUnits,
                // Place the model origin at the bottom center
                centerOrigin = Position(y = -1.0f)
            ) {
                sceneView.planeRenderer.isVisible = true
                isLoading = false
            }
            onAnchorChanged = { node, _ ->
                placeModelButton.isGone = node.isAnchored
            }
            onHitResult = { node, _ ->
                placeModelButton.isGone = !node.isTracking
            }
        }

        sceneView.addChild(modelNode ?: return)
        // Select the model node by default (the model node is also selected on tap)
        sceneView.selectedNode = modelNode
    }

    private fun placeModelNode() {
        modelNode?.anchor()
        placeModelButton.isVisible = false
        sceneView.planeRenderer.isVisible = false
    }
}