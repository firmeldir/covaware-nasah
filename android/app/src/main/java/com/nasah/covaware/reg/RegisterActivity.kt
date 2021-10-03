package com.nasah.covaware.reg

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.mapbox.mapboxsdk.Mapbox
import com.nasah.covaware.CovawareTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.nasah.covaware.MAPBOX_ACCESS_TOKEN

const val REG_SHARED_PREF_NAME = "reg-shared-pref"

const val SHARED_PREF_AGE = "shared-pref-age"
const val SHARED_PREF_DISEASE = "shared-pref-disease"
const val SHARED_PREF_VACCINE = "shared-pref-vaccine"

class RegisterActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			CovawareTheme {
				RegisterScreen()
			}
		}
	}

	@Preview
	@Composable
	private fun RegisterScreen(){
		Scaffold(
			topBar = {
				BottomAppBar {
					Text(
						style = MaterialTheme.typography.h6,
						text = "Enter following data about yourself"
					)
				}
			}
		){

			var age by remember { mutableStateOf("0") }
			var diseas by remember { mutableStateOf(false) }
			var vaccine by remember { mutableStateOf(false) }

			Surface(
				modifier = Modifier.fillMaxSize(),
				color = MaterialTheme.colors.surface
			) {
				Column {
					TextField(
						modifier = Modifier.padding(16.dp),
						value = age,
						onValueChange = { age = it },
						label = { Text("Age") },
						keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
					)

					Column(
						modifier = Modifier.padding(16.dp),
					) {
						Text(
							style = MaterialTheme.typography.body2,
							color = MaterialTheme.colors.primary.copy(alpha = 0.8f),
							text = "Vaccinated"
						)
						Switch(checked = vaccine, onCheckedChange = { vaccine = it })
					}

					Column(
						modifier = Modifier.padding(16.dp),
					) {
						Text(
							style = MaterialTheme.typography.body2,
							color = MaterialTheme.colors.primary.copy(alpha = 0.8f),
							text = "One of the diseases below"
						)
						Switch(checked = diseas, onCheckedChange = { diseas = it })
						Spacer(modifier = Modifier.height(4.dp))
						Text(
							style = MaterialTheme.typography.body2,
							color = MaterialTheme.colors.primary.copy(alpha = 0.8f),
							text = "" +
									"- cancer\n" +
									"- kidney disease\n" +
									"- chronic lung diseases\n" +
									"- dementia or other neurological conditions\n" +
									"- diabetes\n" +
									"- down syndrome\n" +
									"- heart conditions\n" +
									"- HIV infection\n" +
									"- immunocompromised state"
						)
					}

					Button(
						modifier = Modifier.padding(16.dp),
						onClick = {
							getSharedPreferences(REG_SHARED_PREF_NAME, Activity.MODE_PRIVATE).edit {
								putInt(SHARED_PREF_AGE, age.toInt())
								putBoolean(SHARED_PREF_VACCINE, vaccine)
								putBoolean(SHARED_PREF_DISEASE, diseas)
							}
							finish()
						}
					) {
						Text(text = "DONE")
					}
				}
			}
		}
	}
}
