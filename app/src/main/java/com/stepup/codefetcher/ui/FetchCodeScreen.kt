package com.stepup.codefetcher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stepup.codefetcher.FetchCodeViewModel
import com.stepup.codefetcher.R
import com.stepup.codefetcher.Subscribe
import com.stepup.codefetcher.ToolbarTitle
import kotlinx.coroutines.launch

@Composable
fun FetchCodeScreen(
    modifier: Modifier = Modifier,
    viewModel: FetchCodeViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val unknownErrorMessage = stringResource(R.string.unknown_error)
    val scope = rememberCoroutineScope()
    Subscribe(viewModel.errors) {
        scope.launch { snackbarHostState.showSnackbar(it.message ?: unknownErrorMessage) }
    }
    val state by viewModel.state.subscribeAsState(initial = null)
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Fetch Code", Modifier.fillMaxWidth().wrapContentWidth()) }) },
        scaffoldState = rememberScaffoldState(snackbarHostState = snackbarHostState)
    ) { contentPadding ->
        FetchCodeScreen(
            code = state?.code,
            counter = state?.counter,
            isLoading = state?.isLoading ?: true,
            fetchNew = { viewModel.fetchCode() },
            modifier = Modifier.padding(contentPadding).fillMaxSize()
        )
    }
}

@Composable
fun FetchCodeScreen(
    code: String?,
    counter: Int?,
    isLoading: Boolean,
    fetchNew: () -> Unit,
    modifier: Modifier = Modifier
) = Column(
    modifier.wrapContentHeight().padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(stringResource(R.string.response_code_label))
            }
            val noCodeStyle = SpanStyle(color = LocalContentColor.current.copy(alpha = .3f))
            when {
                isLoading -> withStyle(noCodeStyle) {
                    append(stringResource(R.string.loading_text))
                }
                code != null -> append(code)
                else -> withStyle(noCodeStyle) {
                    append(stringResource(R.string.fetch_code_prompt))
                }
            }
        },
        style = MaterialTheme.typography.subtitle1,
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 4.dp)
            .animateContentSize()
            .border(1.dp, MaterialTheme.colors.primary, MaterialTheme.shapes.medium)
            .padding(16.dp)
    )
    Text(
        stringResource(R.string.times_fetched_label, counter ?: 0),
        style = MaterialTheme.typography.caption,
        modifier = Modifier.alpha(if (counter != null) 1f else 0f).padding(bottom = 16.dp)
    )
    Button(
        onClick = fetchNew,
        enabled = !isLoading,
    ) {
        Text(stringResource(R.string.fetch_code_button), Modifier.heightIn(min = 24.dp).wrapContentHeight())
        AnimatedVisibility(visible = isLoading) {
            CircularProgressIndicator(Modifier.padding(start = 8.dp).size(24.dp))
        }
    }
}

@Preview
@Composable
fun FetchCodeScreenPreview() = Surface(color = MaterialTheme.colors.background) {
    Column {
        FetchCodeScreen(
            code = null,
            counter = null,
            isLoading = false,
            fetchNew = {}
        )
        FetchCodeScreen(
            code = null,
            counter = 123,
            isLoading = true,
            fetchNew = {}
        )
        FetchCodeScreen(
            code = "52a5e208-b75e-4b5c-9f23-fae2287162a6",
            counter = 123,
            isLoading = false,
            fetchNew = {}
        )
    }
}