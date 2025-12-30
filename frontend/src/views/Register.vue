<template>
  <div class="bg-white p-8 rounded-xl border border-gray-200 shadow-xl w-full">
    <h2 class="text-3xl font-bold text-primary mb-6 text-center tracking-tight">Create Account</h2>
    <form @submit.prevent="handleRegister" class="space-y-6">
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">Username</label>
        <input 
          v-model="form.username" 
          type="text" 
          class="w-full bg-gray-50 border border-gray-300 rounded-lg p-3 text-gray-900 focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-all"
          placeholder="Choose a username"
          required
        />
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">Password</label>
        <input 
          v-model="form.password" 
          type="password" 
          class="w-full bg-gray-50 border border-gray-300 rounded-lg p-3 text-gray-900 focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-all"
          placeholder="Choose a password"
          required
        />
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">Confirm Password</label>
        <input 
          v-model="form.repeat_password" 
          type="password" 
          class="w-full bg-gray-50 border border-gray-300 rounded-lg p-3 text-gray-900 focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-all"
          placeholder="Confirm your password"
          required
        />
      </div>
      <div v-if="error" class="text-red-500 text-sm text-center bg-red-50 p-2 rounded-lg">{{ error }}</div>
      <button 
        type="submit" 
        :disabled="loading"
        class="w-full bg-primary hover:bg-purple-600 text-white font-bold py-3 px-4 rounded-lg transition-all shadow-md hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {{ loading ? 'Creating Account...' : 'Sign Up' }}
      </button>
      <div class="text-center mt-4">
        <router-link to="/login" class="text-gray-500 hover:text-primary text-sm font-medium transition-colors">
          Already have an account? <span class="underline">Login</span>
        </router-link>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const form = ref({
  username: '',
  password: '',
  repeat_password: ''
})
const loading = ref(false)
const error = ref('')

const handleRegister = async () => {
  if (form.value.password !== form.value.repeat_password) {
    error.value = 'Passwords do not match'
    return
  }
  
  loading.value = true
  error.value = ''
  try {
    await axios.post('/api/register', {
      username: form.value.username,
      password: form.value.password,
      repeat_password: form.value.repeat_password
    })
    // Clear any stale avatar for this username
    try { localStorage.removeItem(`avatar:${form.value.username}`) } catch {}
    
    // Redirect to login or auto login
    router.push('/login')
  } catch (e: any) {
    error.value = e.response?.data?.detail || 'Registration failed'
  } finally {
    loading.value = false
  }
}
</script>
