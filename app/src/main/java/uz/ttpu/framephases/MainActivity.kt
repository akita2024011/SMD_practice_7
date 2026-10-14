package uz.ttpu.framephases

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private const val TAG = "Phases"
private const val DERIVED_TAG = "Derived"
private const val CYCLE_TAG = "Cycle"
private const val SKIP_TAG = "Skip"
private const val PARALLAX_TAG = "Parallax"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize()) { DemoMenu() } } }
    }
}

@Composable
private fun DemoMenu() {
    val demos = listOf("Q4: First frame", "Q5: Composition read", "Q6: Layout read", "Q7: Drawing read", "Q9: Parallax", "Q10: derivedStateOf", "Q11: Restart scope", "Q13: Cyclic layout", "Q14: Custom layout")
    var selected by remember { mutableStateOf(demos[0]) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("FramePhases — Compose phases", style = MaterialTheme.typography.titleLarge)
        Text("Select a demo. Open Logcat and filter by the tag shown in each demo.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        demos.forEach { name ->
            Button(onClick = { selected = name }, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) { Text(name) }
        }
        Divider(Modifier.padding(vertical = 8.dp))
        when (selected) {
            "Q4: First frame" -> LectureTree()
            "Q5: Composition read" -> PaddingDemo()
            "Q6: Layout read" -> OffsetDemo()
            "Q7: Drawing read" -> ColorDemo()
            "Q9: Parallax" -> ParallaxDemo()
            "Q10: derivedStateOf" -> ScrollToTopDemo()
            "Q11: Restart scope" -> ParentDemo()
            "Q13: Cyclic layout" -> CyclicFixedDemo()
            "Q14: Custom layout" -> StackBelowDemo()
        }
    }
}

private fun Modifier.logPhases(name: String): Modifier = this
    .then(Modifier.layout { measurable, constraints ->
        Log.d(TAG, "$name: layout/measure")
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            Log.d(TAG, "$name: layout/place")
            placeable.place(0, 0)
        }
    })
    .drawBehind { Log.d(TAG, "$name: draw") }

@Composable
private fun LectureTree() {
    SideEffect { Log.d(TAG, "LectureTree: composition") }
    Row(Modifier.fillMaxWidth().logPhases("Row"), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(40.dp).background(Color.Gray).logPhases("Image"))
        Column(Modifier.logPhases("Column")) {
            Text("Title text", Modifier.logPhases("Text A"))
            Text("Subtitle", Modifier.logPhases("Text B"))
        }
    }
}

@Composable
private fun PaddingDemo() {
    var padding by remember { mutableStateOf(8.dp) }
    SideEffect { Log.d(TAG, "PaddingDemo: composition") }
    Column {
        Button(onClick = { padding = if (padding == 8.dp) 32.dp else 8.dp }) { Text("Toggle padding") }
        Text("Padding demo", Modifier.padding(padding).logPhases("padding demo"))
    }
}

@Composable
private fun OffsetDemo() {
    var offsetX by remember { mutableStateOf(0.dp) }
    SideEffect { Log.d(TAG, "OffsetDemo: composition") }
    Column {
        Button(onClick = { offsetX = if (offsetX == 0.dp) 80.dp else 0.dp }) { Text("Move") }
        Text("Offset demo", Modifier.offset { IntOffset(offsetX.roundToPx(), 0) }.logPhases("offset demo"))
    }
}

@Composable
private fun ColorDemo() {
    var color by remember { mutableStateOf(Color.Red) }
    SideEffect { Log.d(TAG, "ColorDemo: composition") }
    Column {
        Button(onClick = { color = if (color == Color.Red) Color.Blue else Color.Red }) { Text("Change color") }
        Canvas(Modifier.size(120.dp).logPhases("color demo")) { drawRect(color) }
    }
}

@Composable
private fun ParallaxDemo() {
    val listState = rememberLazyListState()
    val count = remember { intArrayOf(0) }
    SideEffect { Log.d(PARALLAX_TAG, "fast: composition #${++count[0]}") }
    Box(Modifier.fillMaxSize().heightIn(max = 420.dp)) {
        Box(Modifier.fillMaxWidth().height(90.dp).background(Color(0xFF6650A4)).offset { IntOffset(0, listState.firstVisibleItemScrollOffset / 2) })
        LazyColumn(state = listState, modifier = Modifier.padding(top = 100.dp)) {
            items(100) { index -> Text("Item $index", Modifier.fillMaxWidth().background(if (index % 2 == 0) Color(0xFFEDE7F6) else Color.White).padding(16.dp)) }
        }
    }
}

@Composable
private fun ScrollToTopDemo() {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val count = remember { intArrayOf(0) }
    SideEffect { Log.d(DERIVED_TAG, "derived: composition #${++count[0]}") }
    val showButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    Box(Modifier.fillMaxSize().heightIn(max = 420.dp)) {
        LazyColumn(state = listState) { items(200) { index -> Text("Item $index", Modifier.fillMaxWidth().padding(16.dp)) } }
        if (showButton) Button(onClick = { scope.launch { listState.animateScrollToItem(0) } }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) { Text("Top") }
    }
}

@Composable
private fun ParentDemo() {
    var count by remember { mutableIntStateOf(0) }
    SideEffect { Log.d(SKIP_TAG, "Parent: composition") }
    Column {
        Button(onClick = { count++ }) { Text("Add: $count") }
        ChildDemo(count = { count })
        SiblingDemo()
    }
}

@Composable
private fun ChildDemo(count: () -> Int) {
    SideEffect { Log.d(SKIP_TAG, "Child: composition") }
    Text("Count: ${count()}")
}

@Composable
private fun SiblingDemo() {
    SideEffect { Log.d(SKIP_TAG, "Sibling: composition") }
    Text("I never change")
}

@Composable
private fun CyclicFixedDemo() {
    SideEffect { Log.d(CYCLE_TAG, "fixed: composition") }
    Column {
        Box(Modifier.fillMaxWidth().height(120.dp).background(Color(0xFF6650A4)))
        Text("I'm below the image")
    }
}

@Composable
private fun StackBelowDemo() {
    StackBelow(Modifier.fillMaxWidth(), spacing = 8.dp) {
        Box(Modifier.fillMaxWidth().height(120.dp).background(Color(0xFF6650A4)))
        Text("I'm below the image")
    }
}

@Composable
private fun StackBelow(modifier: Modifier = Modifier, spacing: Dp = 8.dp, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { measurable ->
            Log.d("StackBelow", "measure child")
            measurable.measure(loose)
        }
        val spacingPx = spacing.roundToPx()
        val width = (placeables.maxOfOrNull { it.width } ?: 0).coerceAtLeast(constraints.minWidth)
        val totalGaps = (placeables.size - 1).coerceAtLeast(0) * spacingPx
        val height = (placeables.sumOf { it.height } + totalGaps).coerceAtLeast(constraints.minHeight)
        layout(constraints.constrainWidth(width), constraints.constrainHeight(height)) {
            Log.d("StackBelow", "place children")
            var y = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(0, y)
                y += placeable.height + spacingPx
            }
        }
    }
}
