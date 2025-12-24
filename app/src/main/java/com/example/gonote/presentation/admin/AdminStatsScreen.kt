package com.example.gonote.presentation.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gonote.data.model.Note
import com.example.gonote.ui.theme.AccentBlue
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatsScreen(
    statsState: AdminStatsState,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Cities", "Categories", "Activity")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Statistics & Analytics",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (statsState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = AccentBlue
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                when (selectedTab) {
                    0 -> CitiesTab(statsState.citiesWithCount)
                    1 -> CategoriesTab(statsState.categoriesWithCount)
                    2 -> ActivityTab(statsState.allNotes)
                }
            }
        }
    }
}

@Composable
fun CitiesTab(cities: List<com.example.gonote.data.local.dao.CityNoteCount>) {
    if (cities.isEmpty()) {
        EmptyStateMessage("No city data available")
    } else {
        val maxCount = cities.maxOfOrNull { it.count } ?: 1
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Most Active Cities",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(cities.take(10)) { cityData ->
                CityStatBar(
                    city = cityData.city,
                    count = cityData.count,
                    maxCount = maxCount
                )
            }
        }
    }
}

@Composable
fun CityStatBar(
    city: String,
    count: Int,
    maxCount: Int
) {
    val progress = count.toFloat() / maxCount.toFloat()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationCity,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = city,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }
                Text(
                    text = "$count notes",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = AccentBlue,
                trackColor = AccentBlue.copy(alpha = 0.1f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
fun CategoriesTab(categories: List<com.example.gonote.data.local.dao.CategoryCount>) {
    if (categories.isEmpty()) {
        EmptyStateMessage("No category data available")
    } else {
        val total = categories.sumOf { it.count }
        val colors = listOf(
            AccentBlue,
            Color(0xFF4CAF50),
            Color(0xFFFF9800),
            Color(0xFFE91E63),
            Color(0xFF9C27B0),
            Color(0xFF00BCD4)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Category Distribution",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Pie Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    var startAngle = -90f
                    categories.forEachIndexed { index, category ->
                        val sweepAngle = (category.count.toFloat() / total) * 360f
                        drawArc(
                            color = colors[index % colors.size],
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            size = Size(size.width, size.height)
                        )
                        startAngle += sweepAngle
                    }
                    // Inner circle for donut effect
                    drawCircle(
                        color = Color.White,
                        radius = size.width * 0.3f
                    )
                }
                
                Text(
                    text = "$total",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legend
            categories.forEachIndexed { index, category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                colors[index % colors.size],
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = category.category.ifEmpty { "Uncategorized" },
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${category.count} (${(category.count * 100 / total)}%)",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityTab(notes: List<Note>) {
    if (notes.isEmpty()) {
        EmptyStateMessage("No activity data available")
    } else {
        // Group notes by day for the last 30 days
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        val last30Days = (0 until 30).map { daysAgo ->
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
            dayFormat.format(calendar.time) to dateFormat.format(calendar.time)
        }.reversed()

        val notesByDay = notes.groupBy { note ->
            dayFormat.format(Date(note.timestamp))
        }

        val activityData = last30Days.map { (dayKey, dayLabel) ->
            dayLabel to (notesByDay[dayKey]?.size ?: 0)
        }

        val maxCount = activityData.maxOfOrNull { it.second } ?: 1

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Activity (Last 30 Days)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                // Summary cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val totalLast30 = activityData.sumOf { it.second }
                    val activeDays = activityData.count { it.second > 0 }
                    
                    SmallStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Total Notes",
                        value = totalLast30.toString()
                    )
                    SmallStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Active Days",
                        value = activeDays.toString()
                    )
                    SmallStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Avg/Day",
                        value = String.format("%.1f", totalLast30.toFloat() / 30)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Daily Activity",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Activity bars (show last 14 days for better visibility)
            items(activityData.takeLast(14)) { (day, count) ->
                ActivityDayBar(
                    day = day,
                    count = count,
                    maxCount = maxCount.coerceAtLeast(1)
                )
            }
        }
    }
}

@Composable
fun ActivityDayBar(
    day: String,
    count: Int,
    maxCount: Int
) {
    val progress = count.toFloat() / maxCount.toFloat()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = day,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.width(60.dp)
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .background(
                    AccentBlue.copy(alpha = 0.1f),
                    RoundedCornerShape(4.dp)
                )
        ) {
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(AccentBlue, RoundedCornerShape(4.dp))
                )
            }
        }
        
        Text(
            text = count.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (count > 0) AccentBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier
                .width(32.dp)
                .padding(start = 8.dp)
        )
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}





















