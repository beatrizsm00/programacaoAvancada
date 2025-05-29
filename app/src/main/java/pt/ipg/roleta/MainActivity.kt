package pt.ipg.roleta


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Path


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoletaApp()
        }
    }
}
@Composable
fun RoletaCanvasComNomes(
    nomes: List<String>,
    rotationAngle: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Box(
        modifier = modifier.graphicsLayer {
            rotationZ = rotationAngle
        }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (nomes.isEmpty()) return@Canvas

            val total = nomes.size
            val sweepAngle = 360f / total
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            nomes.forEachIndexed { index, _ ->
                val startAngle = index * sweepAngle
                val color = Color(0xFF6750A4)

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                )

                val angleRad = Math.toRadians(startAngle.toDouble())
                val endX = center.x + radius * cos(angleRad).toFloat()
                val endY = center.y + radius * sin(angleRad).toFloat()

                drawLine(
                    color = Color.White,
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 3f
                )
            }
        }

        Box(modifier = Modifier.matchParentSize()) {
            if (nomes.isNotEmpty()) {
                val total = nomes.size
                val sweepAngle = 360f / total
                val angleOffset = -90f

                nomes.forEachIndexed { index, nome ->
                    val angle = Math.toRadians((index * sweepAngle + sweepAngle / 2 + angleOffset).toDouble())
                    val radiusFraction = 0.5f

                    val offset = with(density) {
                        val r = 130.dp.toPx() * radiusFraction
                        IntOffset(
                            x = (r * cos(angle)).toInt(),
                            y = (r * sin(angle)).toInt()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nome,
                            modifier = Modifier.offset { offset },
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoletaApp() {
    var nomeInput by remember { mutableStateOf("") }
    val nomes = remember { mutableStateListOf<String>() }
    var nomeSorteado by remember { mutableStateOf<String?>(null) }

    val rotation = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Roleta de Nomes",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            OutlinedTextField(
                value = nomeInput,
                onValueChange = { nomeInput = it },
                label = { Text("Digite um nome") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(8.dp))


            Button(
                onClick = {
                    if (nomeInput.isNotBlank()) {
                        nomes.add(nomeInput.trim())
                        nomeInput = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Adicionar")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nomes adicionados:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Start)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
            ) {
                items(nomes) { nome ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = nome,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { nomes.remove(nome) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remover",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (nomes.isNotEmpty()) {
                        val selectedIndex = nomes.indices.random()
                        val fullRotations = 5 * 360f
                        val anglePerSector = 360f / nomes.size
                        val targetAngle = fullRotations - (selectedIndex * anglePerSector) - (anglePerSector / 2)
                        coroutineScope.launch {
                            rotation.animateTo(
                                targetAngle,
                                animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing)
                            )
                            nomeSorteado = nomes[selectedIndex]
                        }
                    }
                },
                enabled = nomes.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sortear", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp), // um pouco mais alto para espaço do ponteiro
                contentAlignment = Alignment.TopCenter
            ) {
                // Roleta (por baixo)
                RoletaCanvasComNomes(
                    nomes = nomes,
                    rotationAngle = rotation.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
                if (nomes.isNotEmpty()) {
                    // Ponteiro (fixo no topo)
                    Canvas(
                        modifier = Modifier
                            .size(30.dp)
                            .offset(y = 6.dp) // ajusta para ficar mais próximo da roleta
                    ) {
                        val path = Path().apply {
                            moveTo(size.width / 2f, size.height)
                            lineTo(0f, 0f)
                            lineTo(size.width, 0f)
                            close()
                        }

                        drawPath(path, color = Color.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.height(24.dp))

            nomeSorteado?.let {
                Text(
                    text = "Nome sorteado:",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF009688),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

