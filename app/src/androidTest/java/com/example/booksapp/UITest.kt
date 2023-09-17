package com.example.booksapp

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import kotlinx.coroutines.delay
import org.hamcrest.CoreMatchers.notNullValue

import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
private const val BASIC_SAMPLE_PACKAGE = "com.example.booksapp"
private const val LAUNCH_TIMEOUT = 5000L
private const val STRING_TO_BE_TYPED = "UiAutomator"

@RunWith(AndroidJUnit4::class)
class UITest {

    private lateinit var device: UiDevice
    @Before
    fun startMainActivityFromHomeScreen() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        device.pressHome()

        val launcherPackage: String = device.launcherPackageName
        assertThat(launcherPackage, notNullValue())
        device.wait(
            Until.hasObject(By.pkg(launcherPackage).depth(0)),
            LAUNCH_TIMEOUT
        )

        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(
            BASIC_SAMPLE_PACKAGE)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        device.wait(
            Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)),
            LAUNCH_TIMEOUT
        )
    }

    @Test
    fun searchAndAddBookToFavorites() {
        val favoritesList = device.findObject(By.res(device.currentPackageName, "rv_favorites_list"))
        val startChildrenCount = favoritesList.children.size

        val buttonSearch = device.findObject(By.res(device.currentPackageName, "button_first"))
        buttonSearch.click()
        device.waitForWindowUpdate(device.currentPackageName, 10000)
        val searchBox = device.findObject(By.res(device.currentPackageName, "search_text"))
        searchBox.text = "war"
        val search = device.findObject(By.res(device.currentPackageName, "button_search"))
        search.click()

        val searchResList = device.findObject(By.res(device.currentPackageName, "rv_search_list"))
        device.waitForWindowUpdate(device.currentPackageName, 10000)
        searchResList.children[1].click()
        device.waitForWindowUpdate(device.currentPackageName, 10000)
        val favoritesFab = device.findObject(By.res(device.currentPackageName, "fab"))
        favoritesFab.click()
        device.pressBack()

        val searchResList2 = device.findObject(By.res(device.currentPackageName, "rv_search_list"))
        device.waitForWindowUpdate(device.currentPackageName, 10000)
        searchResList2.children[2].click()
        device.waitForWindowUpdate(device.currentPackageName, 10000)
        val favoritesFab2 = device.findObject(By.res(device.currentPackageName, "fab"))
        favoritesFab2.click()
        device.pressBack()

        device.waitForWindowUpdate(device.currentPackageName, 10000)
        device.pressBack()
        device.waitForWindowUpdate(device.currentPackageName, 10000)

        val favoritesListNew = device.findObject(By.res(device.currentPackageName, "rv_favorites_list"))
        val currentChildren = favoritesListNew.children.size

        assertNotEquals(startChildrenCount, currentChildren)
    }
}
