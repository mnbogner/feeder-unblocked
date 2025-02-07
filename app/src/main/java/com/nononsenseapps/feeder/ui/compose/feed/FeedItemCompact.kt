package com.nononsenseapps.feeder.ui.compose.feed

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.text.WithBidiDeterminedLayoutDirection
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemDateStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemFeedTitleStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemTitleTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.rememberPlaceholderImage
import com.nononsenseapps.feeder.ui.compose.theme.titleFontWeight
import java.net.URL
import java.util.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

val shortDateTimeFormat: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())

@Composable
fun FeedItemCompact(
    item: FeedListItem,
    showThumbnail: Boolean,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onShareItem: () -> Unit,
    onTogglePinned: () -> Unit,
    onToggleBookmarked: () -> Unit,
    dropDownMenuExpanded: Boolean,
    onDismissDropdown: () -> Unit,
    newIndicator: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .height(IntrinsicSize.Min),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .weight(weight = 1.0f, fill = true)
                .requiredHeightIn(min = minimumTouchSize)
                .padding(vertical = 4.dp),
        ) {
            WithBidiDeterminedLayoutDirection(paragraph = item.title) {
                Text(
                    text = item.title,
                    style = FeedListItemTitleTextStyle(),
                    fontWeight = titleFontWeight(item.unread),
                    modifier = Modifier
                        .padding(start = 4.dp, end = 4.dp)
                        .fillMaxWidth(),
                )
            }
            // Want the dropdown to center on the middle text row
            Box {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        val text = buildAnnotatedString {
                            if (item.pubDate.isNotBlank()) {
                                append("${item.pubDate} ‧ ")
                            }
                            withStyle(FeedListItemFeedTitleStyle().toSpanStyle()) {
                                append(item.feedTitle)
                            }
                        }
                        WithBidiDeterminedLayoutDirection(paragraph = text.text) {
                            Text(
                                text = text,
                                style = FeedListItemDateStyle(),
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 4.dp, end = 4.dp),
                            )
                        }
                    }
                }
                DropdownMenu(
                    expanded = dropDownMenuExpanded,
                    onDismissRequest = onDismissDropdown,
                ) {
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onTogglePinned()
                        },
                        text = {
                            Text(
                                text = stringResource(
                                    when (item.pinned) {
                                        true -> R.string.unpin_article
                                        false -> R.string.pin_article
                                    },
                                ),
                            )
                        },
                    )
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onToggleBookmarked()
                        },
                        text = {
                            Text(
                                text = stringResource(
                                    when (item.bookmarked) {
                                        true -> R.string.remove_bookmark
                                        false -> R.string.bookmark_article
                                    },
                                ),
                            )
                        },
                    )
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onMarkAboveAsRead()
                        },
                        text = {
                            Text(
                                text = stringResource(id = R.string.mark_items_above_as_read),
                            )
                        },
                    )
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onMarkBelowAsRead()
                        },
                        text = {
                            Text(
                                text = stringResource(id = R.string.mark_items_below_as_read),
                            )
                        },
                    )
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onShareItem()
                        },
                        text = {
                            Text(
                                text = stringResource(R.string.share),
                            )
                        },
                    )
                }
            }
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                WithBidiDeterminedLayoutDirection(paragraph = item.snippet) {
                    Text(
                        text = item.snippet,
                        style = FeedListItemStyle(),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 4,
                        modifier = Modifier
                            .padding(start = 4.dp, end = 4.dp, bottom = 8.dp)
                            .fillMaxWidth(),
                    )
                }
            }
        }

        if (showThumbnail && (item.imageUrl != null || item.feedImageUrl != null) || item.unread || item.bookmarked || item.pinned) {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.TopEnd,
            ) {
                (item.imageUrl ?: item.feedImageUrl?.toString())?.let { imageUrl ->
                    if (showThumbnail) {
                        val placeholder = rememberPlaceholderImage()
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .listener(
                                    onError = { a, b ->
                                        Log.e("FEEDER_COMPACT", "error ${a.data}", b.throwable)
                                    },
                                )
                                .scale(Scale.FIT)
                                .placeholder(placeholder)
                                .size(200)
                                .error(placeholder)
                                .precision(Precision.INEXACT)
                                .build(),
                            contentDescription = stringResource(id = R.string.article_image),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(64.dp)
                                .fillMaxHeight(),
                        )
                    }
                }
                FeedItemIndicatorColumn(
                    unread = item.unread && newIndicator,
                    bookmarked = item.bookmarked,
                    pinned = item.pinned,
                    modifier = Modifier.padding(
                        top = 4.dp,
                        bottom = 4.dp,
                        end = 4.dp,
                    ),
                    spacing = 4.dp,
                    iconSize = 12.dp,
                )
            }
        } else {
            // Taking Row spacing into account
            Spacer(modifier = Modifier.width(LocalDimens.current.margin - 4.dp))
        }
    }
}

@Immutable
data class FeedListItem(
    val id: Long,
    val title: String,
    val snippet: String,
    val feedTitle: String,
    val unread: Boolean,
    val pubDate: String,
    val imageUrl: String?,
    val link: String?,
    val pinned: Boolean,
    val bookmarked: Boolean,
    val feedImageUrl: URL?,
)

@Composable
@Preview(showBackground = true)
private fun PreviewRead() {
    FeederTheme {
        Surface {
            FeedItemCompact(
                item = FeedListItem(
                    title = "title",
                    snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed One two three hup di too dasf",
                    pubDate = "Jun 9, 2021",
                    unread = false,
                    imageUrl = null,
                    link = null,
                    id = ID_UNSET,
                    pinned = false,
                    bookmarked = false,
                    feedImageUrl = null,
                ),
                showThumbnail = true,
                onMarkAboveAsRead = {},
                onMarkBelowAsRead = {},
                onShareItem = {},
                onTogglePinned = {},
                onToggleBookmarked = {},
                dropDownMenuExpanded = false,
                onDismissDropdown = {},
                newIndicator = true,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun PreviewUnread() {
    FeederTheme {
        Surface {
            FeedItemCompact(
                item = FeedListItem(
                    title = "title",
                    snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed One two three hup di too dasf",
                    pubDate = "Jun 9, 2021",
                    unread = true,
                    imageUrl = null,
                    link = null,
                    id = ID_UNSET,
                    pinned = false,
                    bookmarked = false,
                    feedImageUrl = null,
                ),
                showThumbnail = true,
                onMarkAboveAsRead = {},
                onMarkBelowAsRead = {},
                onShareItem = {},
                onTogglePinned = {},
                onToggleBookmarked = {},
                dropDownMenuExpanded = false,
                onDismissDropdown = {},
                newIndicator = true,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun PreviewWithImage() {
    FeederTheme {
        Surface {
            Box(
                modifier = Modifier.width(400.dp),
            ) {
                FeedItemCompact(
                    item = FeedListItem(
                        title = "title",
                        snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                        feedTitle = "Super Duper Feed One two three hup di too dasf",
                        pubDate = "Jun 9, 2021",
                        unread = true,
                        imageUrl = "blabla",
                        link = null,
                        id = ID_UNSET,
                        pinned = true,
                        bookmarked = true,
                        feedImageUrl = null,
                    ),
                    showThumbnail = true,
                    onMarkAboveAsRead = {},
                    onMarkBelowAsRead = {},
                    onShareItem = {},
                    onTogglePinned = {},
                    onToggleBookmarked = {},
                    dropDownMenuExpanded = false,
                    onDismissDropdown = {},
                    newIndicator = true,
                )
            }
        }
    }
}
