package pl.inpost.shipment.implementation.presentation.shipmentList

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import pl.inpost.design_system.component.appbar.SimpleAppBar
import pl.inpost.design_system.component.button.PrimaryButton
import pl.inpost.design_system.component.divider.HorizontalDivider
import pl.inpost.design_system.theme.InPostTheme
import pl.inpost.shipment.api.model.ShipmentStatus
import pl.inpost.shipment.implementation.R
import pl.inpost.shipment.implementation.presentation.components.ShipmentCard
import pl.inpost.shipment.implementation.presentation.model.DateTimeDisplayable
import pl.inpost.shipment.implementation.presentation.model.ShipmentDisplayable


@Composable
fun ShipmentListScreen(
    viewModel: ShipmentListViewModel,
) {
    val viewState by viewModel.viewState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.onStart()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ShipmentListScreenContent(
        viewState,
        onPullToRefresh = viewModel::refresh,
        archiveShipment = viewModel::onMoreClicked,
        onSnackbarShown = viewModel::onErrorSnackbarShown,
        onArchiveClicked = viewModel::onArchiveClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentListScreenContent(
    viewState: ShipmentList.ViewState,
    onPullToRefresh: () -> Unit,
    archiveShipment: (String) -> Unit,
    onArchiveClicked: () -> Unit,
    onSnackbarShown: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(key1 = viewState.isErrorSnackbarShown) {
        if (viewState.isErrorSnackbarShown) {
            snackbarHostState.showSnackbar(context.getString(R.string.error_occurred))
            onSnackbarShown()
        }
    }
    Scaffold(
        topBar = {
            SimpleAppBar(title = stringResource(id = R.string.app_name))
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = InPostTheme.colorSystem.backgroundPrimary,
        modifier = modifier,
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.background(InPostTheme.colorSystem.backgroundPrimary)
        ) {
            val refreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = viewState.isSwipeRefreshing,
                onRefresh = onPullToRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
                state = refreshState,
                indicator = {
                    Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = viewState.isSwipeRefreshing,
                        state = refreshState,
                        containerColor = InPostTheme.colorSystem.backgroundPrimary,
                        color = InPostTheme.colorSystem.accentPrimary
                    )
                }
            ) {
                if (viewState.isLoading) {
                    CircularProgressIndicator(
                        color = InPostTheme.colorSystem.accentPrimary,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .background(color = InPostTheme.colorSystem.backgroundPrimary)
                            .fillMaxSize()
                    ) {
                        if (viewState.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(id = R.string.no_shipments),
                                    style = InPostTheme.typographySystem.value.copy(textAlign = TextAlign.Center),
                                    modifier = Modifier
                                        .padding(InPostTheme.dimensSystem.x4)
                                        .fillMaxWidth()
                                )
                            }
                        }
                        if (viewState.highlightedShipments.isNotEmpty())
                            item {
                                HorizontalDivider(
                                    title = stringResource(id = R.string.status_ready_to_pickup),
                                    modifier = Modifier.padding(vertical = InPostTheme.dimensSystem.x4)
                                )
                            }
                        itemsIndexed(
                            viewState.highlightedShipments,
                            key = { _, shipment -> shipment.number }
                        ) { index, shipment ->
                            ShipmentCard(
                                shipment = shipment,
                                onDetailsButtonClick = {
                                    archiveShipment(shipment.number)
                                },
                                modifier = Modifier.animateItem()
                            )
                            if (index < viewState.highlightedShipments.size - 1)
                                Spacer(modifier = Modifier.height(InPostTheme.dimensSystem.x4))
                        }
                        if (viewState.shipments.isNotEmpty())
                            item {
                                HorizontalDivider(
                                    title = stringResource(id = R.string.group_other),
                                    modifier = Modifier.padding(vertical = InPostTheme.dimensSystem.x4)
                                )
                            }
                        items(
                            viewState.shipments,
                            key = { it.number }
                        ) { shipment ->
                            ShipmentCard(
                                shipment = shipment,
                                onDetailsButtonClick = {
                                    archiveShipment(shipment.number)
                                },
                                modifier = Modifier.animateItem()
                            )
                            Spacer(modifier = Modifier.height(InPostTheme.dimensSystem.x4))
                        }
                        item {
                            PrimaryButton(
                                title = stringResource(id = R.string.archived),
                                onClick = { onArchiveClicked() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ShipmentListScreenPreview() {
    ShipmentListScreenContent(
        ShipmentList.ViewState.DEFAULT_STATE.copy(
            highlightedShipments = listOf(
                ShipmentDisplayable(
                    number = "123456789",
                    status = ShipmentStatus.READY_TO_PICKUP,
                    statusString = "Ready to collect",
                    sender = "Sender name 1",
                    pickUpDate = DateTimeDisplayable(
                        dayOfWeekShort = "Mon",
                        date = "12.12.2021",
                        time = "12:00"
                    ),
                    expiryDate = DateTimeDisplayable(
                        dayOfWeekShort = "Mon",
                        date = "12.12.2021",
                        time = "12:00"
                    ),
                    storedDate = DateTimeDisplayable(
                        dayOfWeekShort = "Mon",
                        date = "12.12.2021",
                        time = "12:00"
                    ),
                )
            ),
            shipments = listOf(
                ShipmentDisplayable(
                    number = "123456789",
                    status = ShipmentStatus.READY_TO_PICKUP,
                    statusString = "Ready to collect",
                    sender = "Sender name 2",
                    pickUpDate = DateTimeDisplayable(
                        dayOfWeekShort = "Mon",
                        date = "12.12.2021",
                        time = "12:00"
                    ),
                    expiryDate = DateTimeDisplayable(
                        dayOfWeekShort = "Mon",
                        date = "12.12.2021",
                        time = "12:00"
                    ),
                    storedDate = DateTimeDisplayable(
                        dayOfWeekShort = "Mon",
                        date = "12.12.2021",
                        time = "12:00"
                    ),
                )
            )
        ),
        onPullToRefresh = {},
        archiveShipment = {},
        onArchiveClicked = {}
    )
}