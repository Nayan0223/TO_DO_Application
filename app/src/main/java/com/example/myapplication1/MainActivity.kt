package com.example.myapplication1

import android.os.Bundle
import androidx.core.content.edit
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import org.json.JSONArray
import org.json.JSONObject
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication1.ui.theme.MyApplication1Theme

private data class TodoItem(
    val id: Int,
    val title: String,
    val isDone: Boolean = false
)

private enum class TodoFilter(val label: String) {
    All("All"),
    Active("To do"),
    Completed("Done")
}

private const val PREFERENCES_NAME = "todo_list"
private const val TASKS_KEY = "tasks"

private fun loadTodos(context: android.content.Context): List<TodoItem> {
    val savedTasks = context.getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE)
        .getString(TASKS_KEY, null) ?: return emptyList()

    return try {
        val jsonArray = JSONArray(savedTasks)
        List(jsonArray.length()) { index ->
            val task = jsonArray.getJSONObject(index)
            TodoItem(
                id = task.optInt("id", index + 1),
                title = task.optString("title"),
                isDone = task.optBoolean("isDone", false)
            )
        }.filter { it.title.isNotBlank() }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun saveTodos(context: android.content.Context, todos: List<TodoItem>) {
    val jsonArray = JSONArray()
    todos.forEach { todo ->
        jsonArray.put(
            JSONObject()
                .put("id", todo.id)
                .put("title", todo.title)
                .put("isDone", todo.isDone)
        )
    }
    context.getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE).edit {
        putString(TASKS_KEY, jsonArray.toString())
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplication1Theme {
                TodoListScreen()
            }
        }
    }
}

@Composable
private fun TodoListScreen() {
    val context = LocalContext.current.applicationContext
    val todos = remember(context) {
        mutableStateListOf<TodoItem>().apply { addAll(loadTodos(context)) }
    }
    val todoSnapshot = todos.toList()
    LaunchedEffect(todoSnapshot) {
        saveTodos(context, todoSnapshot)
    }
    var taskText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(TodoFilter.All) }

    val remainingCount = todos.count { !it.isDone }
    val completedCount = todos.size - remainingCount
    val progress = if (todos.isEmpty()) 0f else completedCount.toFloat() / todos.size
    val visibleTodos = when (selectedFilter) {
        TodoFilter.All -> todos
        TodoFilter.Active -> todos.filterNot { it.isDone }
        TodoFilter.Completed -> todos.filter { it.isDone }
    }

    fun addTask() {
        val title = taskText.trim()
        if (title.isNotEmpty()) {
            todos.add(TodoItem(id = (todos.maxOfOrNull { it.id } ?: 0) + 1, title = title))
            taskText = ""
            selectedFilter = TodoFilter.All
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "YOUR DAY, YOUR PACE",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Make today\ncount.",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                todos.isEmpty() -> "A fresh start"
                                remainingCount == 0 -> "Everything is done!"
                                else -> "$remainingCount tasks to go"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$completedCount/${todos.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = taskText,
                        onValueChange = { taskText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("What needs doing?") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.surface,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.surface
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { addTask() })
                    )
                    Button(
                        onClick = { addTask() },
                        enabled = taskText.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Text("Add")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TodoFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (visibleTodos.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when {
                                todos.isEmpty() -> "🌱"
                                selectedFilter == TodoFilter.Active -> "✨"
                                else -> "📝"
                            },
                            style = MaterialTheme.typography.displaySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when {
                                todos.isEmpty() -> "Nothing on your list yet"
                                selectedFilter == TodoFilter.Active -> "All caught up!"
                                else -> "No completed tasks yet"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                todos.isEmpty() -> "Add a task above and take it one step at a time."
                                selectedFilter == TodoFilter.Active -> "Enjoy the breathing room."
                                else -> "Finished tasks will show up here."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = visibleTodos, key = { it.id }) { todo ->
                        TodoRow(
                            todo = todo,
                            onCheckedChange = { checked ->
                                val index = todos.indexOfFirst { it.id == todo.id }
                                if (index >= 0) todos[index] = todo.copy(isDone = checked)
                            },
                            onDelete = { todos.removeAll { it.id == todo.id } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoRow(
    todo: TodoItem,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = todo.isDone, onCheckedChange = onCheckedChange)
            Text(
                text = todo.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                textDecoration = if (todo.isDone) TextDecoration.LineThrough else null,
                color = if (todo.isDone) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onDelete) {
                Text("Remove")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoListPreview() {
    MyApplication1Theme {
        TodoListScreen()
    }
}
