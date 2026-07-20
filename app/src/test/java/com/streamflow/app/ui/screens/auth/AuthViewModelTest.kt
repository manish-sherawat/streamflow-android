package com.streamflow.app.ui.screens.auth

import com.streamflow.app.data.repository.MockAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: MockAuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = MockAuthRepository()
        viewModel = AuthViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial auth state is authenticated`() {
        assertTrue(viewModel.isAuthenticated.value)
    }

    @Test
    fun `toggle auth mode switches between login and register`() {
        assertFalse(viewModel.isRegisterMode.value)
        viewModel.toggleAuthMode()
        assertTrue(viewModel.isRegisterMode.value)
    }

    @Test
    fun `submit with blank inputs shows error message`() = runTest {
        viewModel.onEmailChanged("")
        viewModel.onPasswordChanged("")
        viewModel.submit()
        assertEquals("Email and password cannot be empty", viewModel.errorMessage.value)
    }

    @Test
    fun `guest login succeeds`() = runTest {
        viewModel.loginAsGuest()
        assertTrue(viewModel.isAuthenticated.value)
    }
}
