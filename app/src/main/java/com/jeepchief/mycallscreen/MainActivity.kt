package com.jeepchief.mycallscreen

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jeepchief.mycallscreen.data.CallLogVO
import com.jeepchief.mycallscreen.data.CallType
import com.jeepchief.mycallscreen.model.Pref
import com.jeepchief.mycallscreen.model.db.MCRoomDatabase
import com.jeepchief.mycallscreen.model.db.spamfilter.MCSpamFilterEntity
import com.jeepchief.mycallscreen.model.db.spaminfo.MCSpamInfoEntity
import com.jeepchief.mycallscreen.repository.MCSpamFilterRepository
import com.jeepchief.mycallscreen.repository.MCSpamLogRepository
import com.jeepchief.mycallscreen.repository.MCSpamRepository
import com.jeepchief.mycallscreen.ui.theme.Black30
import com.jeepchief.mycallscreen.ui.theme.MyCallScreenTheme
import com.jeepchief.mycallscreen.ui.theme.Purple40
import com.jeepchief.mycallscreen.ui.theme.PurpleGrey40
import com.jeepchief.mycallscreen.util.Logger
import com.jeepchief.mycallscreen.util.TimeUtil
import com.jeepchief.mycallscreen.viewmodel.MCSpamFilterViewModel
import com.jeepchief.mycallscreen.viewmodel.MCSpamFilterViewModelFactory
import com.jeepchief.mycallscreen.viewmodel.MCSpamLogViewModel
import com.jeepchief.mycallscreen.viewmodel.MCSpamLogViewModelFactory
import com.jeepchief.mycallscreen.viewmodel.MCSpamViewModel
import com.jeepchief.mycallscreen.viewmodel.MCSpamViewModelFactory
import com.jeepchief.mycallscreen.viewmodel.MCStateViewModel
import com.jeepchief.mycallscreen.viewmodel.MCViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val spamInfoRepository: MCSpamRepository by lazy {
        val dao = MCRoomDatabase.getInstance(this).getSpamInfoDao()
        MCSpamRepository(dao)
    }
    private val spamInfoViewModel: MCSpamViewModel by viewModels {
        MCSpamViewModelFactory(spamInfoRepository)
    }

    private val spamLogRepository: MCSpamLogRepository by lazy {
        val dao = MCRoomDatabase.getInstance(this).getSpamLogDao()
        MCSpamLogRepository(dao)
    }
    private val spamLogViewModel: MCSpamLogViewModel by viewModels {
        MCSpamLogViewModelFactory(spamLogRepository)
    }

    private val spamFilterRepository: MCSpamFilterRepository by lazy {
        val dao = MCRoomDatabase.getInstance(this).getSpamFilterDao()
        MCSpamFilterRepository(dao)
    }
    private val spamFilterViewModel: MCSpamFilterViewModel by viewModels {
        MCSpamFilterViewModelFactory(spamFilterRepository)
    }

    private val stateViewModel: MCStateViewModel by viewModels()
    private val viewModel: MCViewModel by viewModels()

    private lateinit var requestRoleLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initLauncher()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestRole()
        }

        setContent {
            val isPermissionGranted by stateViewModel.isPermissionGranted.collectAsState()
//            val isSpamLogShowing by stateViewModel.isSpamLogShowing.collectAsState()
            val isSpamLogShowing = intent.getBooleanExtra("isSpamLogShowing", false)
            if(isPermissionGranted) {
                MyCallScreenTheme {
                    if(isSpamLogShowing) {
                        intent.getStringExtra("incomingNumber")?.let { incomingNumber ->
                            SpamLog(incomingNumber, spamLogViewModel) {
                                finishAffinity()
                            }
                        } ?: run {
                            Toast.makeText(this, "알수없는 에러가 발생하여 앱을 재시작합니다.", Toast.LENGTH_SHORT).show()
                            startActivity(
                                Intent(this@MainActivity, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            )
                        }
                    }
                    else {
                        val navHost = rememberNavController()
                        val navBackStackEntity by navHost.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntity?.destination?.route
                        val isAddSpamDialogShowing by stateViewModel.isAddSpamDialogShowing.collectAsState()
                        val isSpamFilterDialogShowing by stateViewModel.isSpamFilterDialogShowing.collectAsState()

                        Scaffold(
                            bottomBar = { BottomNavigationBar(navController = navHost, stateViewModel) },
                            floatingActionButton = {
                                if(currentRoute == MCScreenRoute.SpamList.route) {
                                    ExpandedFab(stateViewModel)
                                }
                            }
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding()))
                                AppNavHost(
                                    navController = navHost,
                                    paddingValues = it,
                                    viewModel = viewModel,
                                    stateViewModel = stateViewModel,
                                    spamViewModel = spamInfoViewModel,
                                    spamFilterViewModel = spamFilterViewModel
                                )
                            }
                        }

                        // 스팸 추가 Dialog
                        if(isAddSpamDialogShowing && currentRoute == MCScreenRoute.SpamList.route) {
                            AddSpamDialog(stateViewModel = stateViewModel, spamViewModel = spamInfoViewModel)
                        }

                        // 스팸 필터 추가 Dialog
                        if(isSpamFilterDialogShowing) {
                            SpamFilterDialog(stateViewModel = stateViewModel, spamFilterViewModel = spamFilterViewModel)
                        }
                    }

                    BackHandler {
                        finishAffinity()
                    }
                }
            }
        }
    }

    override fun onResume() {
        Logger.log("onResume()")
        super.onResume()
    }

    private fun initLauncher() {
        val requestMoveToSettingLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            requestPermission()
        }

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionMap ->
            permissionMap.forEach { permission, isGranted ->
                if(!isGranted) {
                    AlertDialog.Builder(this)
                        .setMessage("앱을 사용하려면 권한이 필요합니다,\n설정으로 이동하여 권한을 허용해주세요.")
                        .setPositiveButton("설정 이동") { _, _ ->
                            requestMoveToSettingLauncher.launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                setData(Uri.parse("package:${packageName}"))
                            })
                        }
                        .setNegativeButton("앱 종료") { _, _ ->
                            finishAffinity()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    stateViewModel.setIsPermissionGranted(true)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestRole() {
        Logger.log("requestRole()")

        val roleManager = getSystemService(RoleManager::class.java)
        fun request() = requestRoleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))

        requestRoleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Logger.log("it result >> ${it.resultCode}")

            if(it.resultCode == RESULT_CANCELED) {
                AlertDialog.Builder(this)
                    .setMessage("기본 앱으로 지정하셔야 정상작동할 수 있습니다.")
                    .setPositiveButton("설정하기") { _, _ ->
                        request()
                    }
                    .setNegativeButton("앱 종료") { _, _ -> finishAffinity() }
                    .setCancelable(false)
                    .show()
            } else requestPermission()
        }

        request()
    }

    private fun requestPermission() {
        Logger.log("requestPermission()")

        if(checkPermission()) {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALL_LOG
            ))
        } else {
            stateViewModel.setIsPermissionGranted(true)
        }
    }

    private fun checkPermission(): Boolean {
        listOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG
        ).forEach { permission ->
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return true
        }

        return false
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, viewModel: MCStateViewModel) {
    val selectedItem by viewModel.selectedItem.collectAsState()

    NavigationBar {
        MCScreenRoute.entries.forEachIndexed { index, route ->
            NavigationBarItem(
                selected = selectedItem == index,
                label = { Text(text = route.label) },
                icon = { Icon(route.icon, null) },
                onClick = {
                    val prevRoute = MCScreenRoute.entries.get(selectedItem)
                    viewModel.setSelectedItem(index)
                    navController.navigate(route.route) {
                        popUpTo(prevRoute.route) { inclusive = true }
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CallLogScreen(
    viewModel: MCViewModel,
    stateViewModel: MCStateViewModel,
    spamViewModel: MCSpamViewModel
) {
    val callLogList = viewModel.getCallLog(LocalContext.current)
    val isAddSpamDialogShowing by stateViewModel.isAddSpamDialogShowing.collectAsState()
    val isLogMenuDialogShowing by stateViewModel.isLogMenuDialogShowing.collectAsState()
    var selectedNumber by remember { mutableStateOf<CallLogVO>(CallLogVO()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(callLogList.size) {
            val vo = callLogList[it]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clickable {
                        selectedNumber = vo
                        stateViewModel.setIsLogMenuDialogShowing(true)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                val name = if(vo.name == "미등록번호") "[미등록번호] ${vo.number}"
                                else vo.name

                Column(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp)
                        .width(70.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val iconImage = when (vo.type) {
                        CallType.INCOMING -> painterResource(id = R.drawable.call_received_24px)
                        CallType.OUTGOING -> painterResource(id = R.drawable.call_made_24px)
                        CallType.MISSED -> painterResource(id = R.drawable.call_missed_24px)
                        CallType.REJECT -> painterResource(id = R.drawable.phone_disabled_24px)
                        CallType.SPAM -> painterResource(id = R.drawable.phone_missed_24px)
                        CallType.UNSPECIFIED -> painterResource(id = R.drawable.question_mark_24px)
                    }
                    Icon(iconImage, null)
                    Text(text = "${vo.type.desc}")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "$name\r\n(${TimeUtil.convertSecToMin(vo.duration)})", modifier = Modifier.padding(end = 20.dp))
            }
        }

    }

    if (isLogMenuDialogShowing) {
        LogMenuDialog(stateViewModel, selectedNumber, spamViewModel)
    }

    if(isAddSpamDialogShowing) {
        AddSpamDialog(stateViewModel, spamViewModel, selectedNumber.number, selectedNumber.name)
    }
}



@Composable
fun SpamListScreen(spamViewModel: MCSpamViewModel) {
    var isSpamListInit by remember { mutableStateOf(false) }
    val spamList by spamViewModel.allSpamInfo.collectAsState().also { isSpamListInit = true }
    var isDeleteSpamDialogShowing by remember { mutableStateOf(false) }
    var selectedNumber by remember { mutableStateOf(MCSpamInfoEntity()) }

    if(!isSpamListInit) {
        CircularProgress()
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if(spamList.isEmpty()) {
            Text(text = "SpamList empty ..")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(spamList.size) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clickable {
                                isDeleteSpamDialogShowing = true
                                selectedNumber = spamList[it]
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, Modifier.padding(start = 20.dp, end = 20.dp))
                        Text(
                            text = """
                                ${spamList[it].number}
                                (${spamList[it].name})
                            """.trimIndent(),
//                            modifier = Modifier.padding(start = 20.dp)
                        )
                    }
                }
            }
        }

        if(isDeleteSpamDialogShowing) {
            MyDialog(
                onDismissRequest = { isDeleteSpamDialogShowing = false },
                title = "스팸 삭제",
                confirmButtonText = "삭제",
                confirmCallback = {
                    spamViewModel.delete(selectedNumber.number)
                    isDeleteSpamDialogShowing = false
                },
                dismissButtonText = "취소",
                dismissCallback = { isDeleteSpamDialogShowing = false }
            ) {
                Text(text = "${selectedNumber.number}(${selectedNumber.name}) 를 스팸목록에서 삭제하시겠습니까?")
            }
        }
    }
}

@Composable
fun SettingScreen() {
    var isRejectCallVisible by remember { mutableStateOf(Pref.getBoolean(Pref.DISALLOW_CALL)) }
    var isRejectNotifyVisible by remember { mutableStateOf(Pref.getBoolean(Pref.SKIP_NOTIFICATION)) }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 전화거부
        SettingItem(
            SettingItemDesc.DISALLOW_CALL,
            Pref.getBoolean(Pref.DISALLOW_CALL)
        ) {
            Pref.setValue(Pref.DISALLOW_CALL, it)
            isRejectCallVisible = it

            // 자동 해제
            if(!it) {
                Pref.setValue(Pref.REJECT_CALL, it)
            }
        }
        if(isRejectCallVisible) {
            // 통화
            SettingItem(
                SettingItemDesc.REJECT_CALL,
                Pref.getBoolean(Pref.REJECT_CALL)
            ) {
                Pref.setValue(Pref.REJECT_CALL, it)
            }
            // 통화기록생략
            SettingItem(
                SettingItemDesc.SKIP_CALL_LOG,
                Pref.getBoolean(Pref.SKIP_CALL_LOG)
            ) {
                Pref.setValue(Pref.SKIP_CALL_LOG, it)
            }
            // 통화알림생략
            SettingItem(
                SettingItemDesc.SKIP_NOTIFICATION,
                Pref.getBoolean(Pref.SKIP_NOTIFICATION)
            ) {
                Pref.setValue(Pref.SKIP_NOTIFICATION, it)
                isRejectNotifyVisible = it

                // 자동 해제
                if(!it) {
                    Pref.setValue(Pref.REJECT_NOTIFICATION, it)
                }
            }
            // 차단알림
            if(isRejectNotifyVisible) {
                SettingItem(
                    SettingItemDesc.REJECT_NOTIFICATION,
                    Pref.getBoolean(Pref.REJECT_NOTIFICATION)
                ) {
                    Pref.setValue(Pref.REJECT_NOTIFICATION, it)
                }
            }

            // 번호 필터링
//            SettingItem(
//                SettingItemDesc.SPAM_FILTER,
//                Pref.getBoolean(Pref.SPAM_FILTER)
//            ) {
////                Pref.setValue(Pref.SPAM_FILTER, it)
//                stateViewModel.setIsSpamFilterDialogShowing(true)
//            }
        }
    }
}

@Composable
//@Preview(name = "SettingItem")
fun SettingItem(settingItem: SettingItemDesc, checked: Boolean, onClick: (Boolean) -> Unit) {
    var checkedState by remember { mutableStateOf(checked) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(
//                enabled = enabled,
                onClick = {
                    onClick(!checkedState)
                    checkedState = checkedState.not()
                }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 20.dp)
        ) {
            Text(text = settingItem.title)
            Text(text = settingItem.desc, fontStyle = FontStyle.Italic, fontSize = TextUnit(12f, TextUnitType.Sp))
        }

        Checkbox(
            checked = checkedState,
            onCheckedChange = {
                onClick(it)
                checkedState = checkedState.not()
            },
            modifier = Modifier.padding(end = 20.dp)
        )
    }
}

enum class MCScreenRoute(val route: String, val icon: ImageVector, val label: String) {
    CallLog("CallLog", Icons.Filled.Call, "통화기록"),
    SpamList("SpamList", Icons.Filled.Warning, "스팸목록"),
    Setting("Setting", Icons.Filled.Settings, "환경설정")
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    viewModel: MCViewModel,
    stateViewModel: MCStateViewModel,
    spamViewModel: MCSpamViewModel,
    spamFilterViewModel: MCSpamFilterViewModel
) {
    NavHost(
        navController = navController,
        startDestination = MCScreenRoute.CallLog.route,
    ) {
        composable(MCScreenRoute.CallLog.route) {
            CallLogScreen(viewModel = viewModel, stateViewModel = stateViewModel, spamViewModel = spamViewModel)
        }
        composable(MCScreenRoute.SpamList.route) {
            SpamListScreen(spamViewModel = spamViewModel)
        }
        composable(MCScreenRoute.Setting.route) {
            SettingScreen()
        }
    }
}

@Composable
fun AddSpamDialog(stateViewModel: MCStateViewModel, spamViewModel: MCSpamViewModel, selectedNumber: String = "", selectedName: String = "") {
    var numberEditText by remember { mutableStateOf(selectedNumber) }
    var nameEditText by remember { mutableStateOf(selectedName) }
    val context = LocalContext.current

    MyDialog(
        onDismissRequest = { stateViewModel.setIsAddSpamDialogShowing(false) },
        title = "스팸 추가",
        confirmButtonText = "추가",
        confirmCallback = {
            if(nameEditText.isBlank() || numberEditText.isBlank()) {
                Toast.makeText(context, "빈 값을 입력할 수 없습니다!", Toast.LENGTH_SHORT).show()
                return@MyDialog
            }
            Toast.makeText(context, "스팸목록에 저장되었습니다.", Toast.LENGTH_SHORT).show()
            spamViewModel.insert(MCSpamInfoEntity(name = nameEditText.trim(), number = numberEditText.trim()))
            stateViewModel.setIsAddSpamDialogShowing(false)
        },
        dismissButtonText = "닫기",
        dismissCallback = { stateViewModel.setIsAddSpamDialogShowing(false) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = nameEditText,
                onValueChange = {
                    nameEditText = it
                },
                label = { Text(text = "이름") }
            )
            OutlinedTextField(
                value = numberEditText,
                onValueChange = {
                    numberEditText = it
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(text = "전화번호") }
            )
        }
    }
}

@Composable
fun LogMenuDialog(stateViewModel: MCStateViewModel, selectedNumber: CallLogVO, spamViewModel: MCSpamViewModel) {
    val menuArray = arrayOf("스팸추가", "전화걸기")
    val context = LocalContext.current

    MyDialog(
        onDismissRequest = { stateViewModel.setIsLogMenuDialogShowing(false) },
        title = "추가 동작",
        dismissButtonText = "닫기",
        dismissCallback = { stateViewModel.setIsLogMenuDialogShowing(false) }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            items(menuArray.size) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = menuArray[it],
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clickable {
                                when (it) {
                                    0 -> {
                                        // 스팸차단

                                        stateViewModel.run {
                                            setIsLogMenuDialogShowing(false)
                                            setIsAddSpamDialogShowing(true)
                                        }
                                    }

                                    1 -> {
                                        // 전화걸기
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("tel:${selectedNumber.number}")
                                            )
                                        )
                                        stateViewModel.setIsLogMenuDialogShowing(false)
                                    }
                                }
                            },
//                    textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 버튼을 한 개만 사용할 경우 confirmButtonText 생략 가능
 */
@Composable
fun MyDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButtonText: String? = null,
    confirmCallback: () -> Unit = {},
    dismissButtonText: String,
    dismissCallback: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            confirmButtonText?.let { confirmButtonText ->
                Button(onClick = confirmCallback) {
                    Text(text = confirmButtonText)
                }
            }
        },
        dismissButton = {
            Button(onClick = dismissCallback) {
                Text(text = dismissButtonText)
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(text = title)
        },
        text = content
    )
}

@Composable
fun SpamLog(number: String, spamLogViewModel: MCSpamLogViewModel, onBack: () -> Unit) {
    spamLogViewModel.getSpamLogWithNum(number)

    val spamLogList by spamLogViewModel.spamLogWithNum.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(spamLogList.size) {
            val vo = spamLogList[it]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(id = R.drawable.phone_missed_24px), null, modifier = Modifier.padding(end = 20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "${TimeUtil.convertMillToDate(vo.time)}\r\n${vo.name}\r\n${vo.number}", modifier = Modifier.padding(end = 20.dp))
            }
        }
    }

    BackHandler(onBack = onBack)
}

@Composable
fun SpamFilterDialog(
    stateViewModel: MCStateViewModel,
    spamFilterViewModel: MCSpamFilterViewModel
) {
    var isLoad by remember { mutableStateOf(false) }
    var isSpamFilterEnable by remember { mutableStateOf(Pref.getBoolean(Pref.SPAM_FILTER)) }
    val filterList by spamFilterViewModel.allSpamFilter.collectAsState()
    var textChange by remember { mutableStateOf("") }
    val checkedState = mutableStateMapOf<String, Boolean>().apply {
        filterList.forEach { this[it.filterNumber] = false }
    }.also { isLoad = true }
    
    MyDialog(
        onDismissRequest = { stateViewModel.setIsSpamFilterDialogShowing(false) },
        title = "번호 필터링",
        dismissButtonText = "닫기",
        dismissCallback = { stateViewModel.setIsSpamFilterDialogShowing(false) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 5.dp, end = 5.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "번호 필터링 사용", modifier = Modifier.weight(1f))
                Switch(checked = isSpamFilterEnable, onCheckedChange = {
                    isSpamFilterEnable = it
                    Pref.setValue(Pref.SPAM_FILTER, it)
                })
            }
            Spacer(modifier = Modifier.height(10.dp))
            if(isSpamFilterEnable) {
                if(filterList.isEmpty()) {
//                    LaunchedEffect(key1 = Unit) {
//                        delay(200)
//                    }
                    Text(
                        text = "등록된 필터링 번호가 없습니다.",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else {
                    if(!isLoad) {
                        CircularProgress()
                        return@MyDialog
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filterList.size) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        var isChecked =
                                            checkedState[filterList[it].filterNumber] ?: false
                                        checkedState[filterList[it].filterNumber] = !isChecked
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = filterList[it].filterNumber, modifier = Modifier.weight(1f))
                                Checkbox(checked = checkedState[filterList[it].filterNumber] ?: false, onCheckedChange = { isChecked ->
                                    checkedState[filterList[it].filterNumber] = isChecked
                                })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = textChange,
                    onValueChange = { textChange = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text(text = "필터링 대상 입력") }
                )
//                TextField(
//                    value = textChange,
//                    onValueChange = { textChange = it },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = {
                    spamFilterViewModel.insert(
                        MCSpamFilterEntity(filterNumber = textChange)
                    )
                    checkedState.put(textChange, false)
                    textChange = ""
                },
                    modifier = Modifier.fillMaxWidth()) {
                    Text(text = "추가")
                }
                if(checkedState.containsValue(true)) {
                    Button(onClick = {
                        spamFilterViewModel.delete(
                            *checkedState.filter { it.value }.toList().map { it.first }.toTypedArray().also { Logger.log("array > ${it.contentToString()}") }
                        )
                    },
                        modifier = Modifier.fillMaxWidth()) {
                        Text(text = "삭제")
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandedFab(stateViewModel: MCStateViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.End
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(onClick = {
                expanded = !expanded
                stateViewModel.setIsAddSpamDialogShowing(true)
            }) {
//                Icon(Icons.Default.Add, null)
                Text(text = "스팸 추가")
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(onClick = {
                expanded = !expanded
                stateViewModel.setIsSpamFilterDialogShowing(true)
            }) {
//                Icon(Icons.Default.Close, null)
                Text(text = "필터 추가")
            }
        }
        Button(onClick = {
            expanded = !expanded
//            Toast.makeText(context, "expanded > $expanded", Toast.LENGTH_SHORT).show()
        }) {
            Icon(if(expanded) Icons.Default.Close else Icons.Default.Menu, null)
        }
    }
}

@Composable
fun CircularProgress() {
    val activity = (LocalContext.current as MainActivity)
    CircularProgressIndicator()
    LaunchedEffect(Unit) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        delay(3000)
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}

enum class SettingItemDesc(val title: String, val desc: String) {
    DISALLOW_CALL ("전화거부", "스팸목록에 있는 번호가 수신되면 전화를 차단합니다."),
    REJECT_CALL ("즉시거부", "발신자에게 전화를 즉시거부한 것처럼 보이게 합니다."),
    SKIP_CALL_LOG ("통화기록생략", "통화기록에 차단한 전화를 기록하지 않습니다."),
    SKIP_NOTIFICATION ("통화알림생략", "차단한 전화의 부재중 알림을 수신하지 않습니다."),
    REJECT_NOTIFICATION ("차단알림", "차단한 전화를 전용 알림으로 표시합니다."),
//    SPAM_FILTER("번호 필터링", "지정한 번호가 포함된 전화를 차단합니다.")
}

/**
 * StatusBar, Navigation 색상 변경
 */
@Composable
fun SetSystemBarColor(
    statusBarColor: Color = Color.Transparent,
    navigationBarColor: Color = Color.Transparent,
    useDarkIcons: Boolean = false
) {
    val activity = LocalContext.current as Activity

    val window = activity.window
    SideEffect {
        window.statusBarColor = statusBarColor.toArgb()
        window.navigationBarColor
    }
}