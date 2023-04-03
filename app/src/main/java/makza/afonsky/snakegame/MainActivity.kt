package makza.afonsky.snakegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import makza.afonsky.snakegame.ui.theme.SnakeGameTheme
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnakeGameTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Snake()
                }
            }
        }
    }
}

data class State(val food:Pair<Int, Int>, val snake: List<Pair<Int, Int>>)

class Game(private val scope: CoroutineScope){

    private val mutex = Mutex()

    private val mutableState = MutableStateFlow(State(food = Pair(5,5), snake = listOf(Pair(7,7))))
    val state: Flow<State> = mutableState

    var move = Pair(1,0)
    set(value){
        scope.launch {
            mutex.withLock {
                field = value
            }
        }
    }

    init {
        scope.launch {
            var snakeLength = 4

            while (true){
                delay(150)
                mutableState.update {
                    val newPosition = it.snake.first().let { poz ->
                        mutex.withLock {
                            Pair(
                                (poz.first + move.first + BOARD_SIZE) % BOARD_SIZE,
                                (poz.second + move.second) % BOARD_SIZE
                            )
                        }
                    }

                    if (newPosition == it.food){
                        snakeLength++
                    }
                    if (it.snake.contains(newPosition)){
                        snakeLength = 4
                    }
                    it.copy(
                        food = if (newPosition == it.food) Pair(
                            Random().nextInt(BOARD_SIZE),
                            Random().nextInt(BOARD_SIZE)
                        ) else it.food,
                        snake = listOf(newPosition) + it.snake.take(snakeLength - 1)
                    )
                }
            }
        }
    }

    companion object {
        const val BOARD_SIZE = 16
    }


}

@Composable
fun Snake() {

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SnakeGameTheme {
        Snake()
    }
}