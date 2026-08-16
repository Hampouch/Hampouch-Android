package com.example.hampouch.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

@Composable
fun BottomNavBar(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HPWhite)
            .border(width = 1.dp, color = HPGray4)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavSlot(
                item = BottomNavItem.HOME,
                selected = selectedItem == BottomNavItem.HOME,
                onClick = { onItemSelected(BottomNavItem.HOME) },
                modifier = Modifier.weight(1f)
            )
            BottomNavSlot(
                item = BottomNavItem.HAM_BATTLE,
                selected = selectedItem == BottomNavItem.HAM_BATTLE,
                onClick = { onItemSelected(BottomNavItem.HAM_BATTLE) },
                modifier = Modifier.weight(1f)
            )
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                AddActionButton(onClick = onAddClick)
            }
            BottomNavSlot(
                item = BottomNavItem.COMMUNITY,
                selected = selectedItem == BottomNavItem.COMMUNITY,
                onClick = { onItemSelected(BottomNavItem.COMMUNITY) },
                modifier = Modifier.weight(1f)
            )
            BottomNavSlot(
                item = BottomNavItem.MY_PAGE,
                selected = selectedItem == BottomNavItem.MY_PAGE,
                onClick = { onItemSelected(BottomNavItem.MY_PAGE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomNavSlot(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (selected) HPBlack else HPText
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (selected) item.icon else item.outlineIcon,
            contentDescription = stringResource(item.labelResId),
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(item.labelResId),
            style = MaterialTheme.typography.labelMedium,
            color = tint
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(width = 20.dp, height = 3.dp)
                .background(if (selected) HPMain else Color.Transparent, RoundedCornerShape(50))
        )
    }
}

@Composable
private fun AddActionButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(HPMain, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(R.string.cd_add_expense),
            tint = HPWhite
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomNavBarPreview() {
    var selected by remember { mutableStateOf(BottomNavItem.HOME) }
    HampouchTheme {
        BottomNavBar(
            selectedItem = selected,
            onItemSelected = { selected = it },
            onAddClick = {}
        )
    }
}
