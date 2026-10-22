package app.lawnchair.ui.preferences.destinations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.lawnchair.ui.preferences.components.controls.ListPreference
import app.lawnchair.ui.preferences.components.controls.ListPreferenceEntry
import app.lawnchair.ui.preferences.components.layout.PreferenceGroup
import app.lawnchair.ui.preferences.components.layout.PreferenceLayout
import app.lawnchair.ui.preferences.LocalIsExpandedScreen
import com.android.launcher3.R
import com.android.launcher3.anim.SmoothSpringPresets
import androidx.compose.ui.res.stringResource

@Composable
fun SmoothMotionPreferences(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("smooth_motion_preferences", 0)
    }
    var profile by remember {
        mutableStateOf(prefs.getString("profile", "smooth") ?: "smooth")
    }
    var stiffness by remember {
        mutableFloatStateOf(prefs.getFloat("stiffness", 260f))
    }
    var damping by remember {
        mutableFloatStateOf(prefs.getFloat("damping", 0.8f))
    }

    fun save(nextProfile: String, nextStiffness: Float, nextDamping: Float) {
        prefs.edit()
            .putString("profile", nextProfile)
            .putFloat("stiffness", nextStiffness)
            .putFloat("damping", nextDamping)
            .apply()
        SmoothSpringPresets.load(context)
    }

    PreferenceLayout(
        label = "Smooth Motion",
        backArrowVisible = !LocalIsExpandedScreen.current,
        modifier = modifier,
    ) {
        PreferenceGroup(
            heading = "Animationsstil",
            description = "Wähle das Bewegungsgefühl des Workspace.",
        ) {
            ListPreference(
                label = "Profil",
                value = profile,
                entries = listOf(
                    ListPreferenceEntry("smooth") { "Smooth – weich und flüssig" },
                    ListPreferenceEntry("snappy") { "Snappy – schnell und knackig" },
                    ListPreferenceEntry("gentle") { "Gentle – sanft und ruhig" },
                    ListPreferenceEntry("custom") { "Custom – eigene Werte" },
                ),
                onValueChange = { selected ->
                    profile = selected
                    when (selected) {
                        "snappy" -> {
                            stiffness = 420f
                            damping = 0.72f
                        }
                        "gentle" -> {
                            stiffness = 160f
                            damping = 0.9f
                        }
                        "smooth" -> {
                            stiffness = 260f
                            damping = 0.8f
                        }
                        else -> Unit
                    }
                    save(profile, stiffness, damping)
                },
            )
        }

        PreferenceGroup(
            heading = "Feinabstimmung",
            description = "Änderungen werden auf diesem Gerät gespeichert.",
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text("Federstärke: ${stiffness.toInt()}")
                Slider(
                    value = stiffness,
                    onValueChange = {
                        stiffness = it
                        profile = "custom"
                        save(profile, stiffness, damping)
                    },
                    valueRange = 50f..1000f,
                )
                Text("Dämpfung: ${"%.2f".format(java.util.Locale.ROOT, damping)}")
                Slider(
                    value = damping,
                    onValueChange = {
                        damping = it.coerceIn(0.1f, 0.99f)
                        profile = "custom"
                        save(profile, stiffness, damping)
                    },
                    valueRange = 0.1f..0.99f,
                )
                Text("Höhere Federstärke reagiert schneller. Mehr Dämpfung reduziert das Nachfedern.")
            }
        }
    }
}
