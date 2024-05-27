package fr.vocaltech.location.navigation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import fr.vocaltech.location.models.Coordinates
import fr.vocaltech.location.models.Position
import fr.vocaltech.location.ui.theme.LocationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ListScreen(
    navController: NavHostController,
    updatePositionsByUserId: (String) -> Unit,
    positionsByUserId: List<Position>,
    deletePositionsByUserId: (String) -> Unit
) {
    LocationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ListContent(
                navController,
                updatePositionsByUserId,
                positionsByUserId,
                deletePositionsByUserId
            )
        }
    }
}

@Composable
fun ListContent(
    navController: NavHostController,
    updatePositionsByUserId: (String) -> Unit,
    positionsByUserId: List<Position>,
    deletePositionsByUserId: (String) -> Unit
) {
    val openDeleteDialog = remember { mutableStateOf(false) }

    when {
        openDeleteDialog.value -> {
            DeleteDialog(
                onDismissRequest = { openDeleteDialog.value = false },
                onConfirmation = {
                    openDeleteDialog.value = false
                    deletePositionsByUserId("userId")
                    updatePositionsByUserId("userId")
                },
                dialogTitle = "Delete all positions ?" ,
                dialogText = "Are you sure you want to permanently delete all positions for this user ?",
                icon = Icons.Default.Delete
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                updatePositionsByUserId("userId")
            }) {
                Text("Update")
            }

            Button(
                enabled = positionsByUserId.isNotEmpty(),
                onClick = {
                    openDeleteDialog.value = true
                }
            ) {
                Text("Clear")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()

        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text =  if (positionsByUserId.isNotEmpty())
                            "My positions"
                        else
                            "No positions available.",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(positionsByUserId) { index, currentPosition ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            top = 16.dp,
                            bottom = 0.dp
                        )
                ) {
                    Text(
                        text =  "index: #$index\n" +
                                "lat: ${currentPosition.coordinates.latitude} / lng: ${currentPosition.coordinates.longitude} \n" +
                                "timestamp: " +
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE)
                                        .format(Date(currentPosition.timestamp)) +
                                " [${currentPosition.timestamp}]",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(imageVector = icon, contentDescription = "Delete icon")
        },
        title = {
            Text(dialogTitle)
        },
        text = {
            Text(dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ListContentPreviewEmptyList() {
    LocationTheme {
        ListContent(
            navController = rememberNavController(),
            updatePositionsByUserId = {},
            positionsByUserId = emptyList(),
            deletePositionsByUserId = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListContentPreviewNotEmptyList() {
    LocationTheme {
        ListContent(
            navController = rememberNavController(),
            updatePositionsByUserId = {},
            positionsByUserId = listOf(
                Position(Coordinates(45.4, 1.78), 178984, "trackId", "userId"),
                Position(Coordinates(45.4, 1.78), 178984, "trackId", "userId")
            ),
            deletePositionsByUserId = {}
        )
    }
}