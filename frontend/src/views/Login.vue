<template>
  <div class="bg-white p-8 rounded-xl border border-gray-200 shadow-xl w-full">
    <h2 class="text-3xl font-bold text-primary mb-6 text-center tracking-tight">Login</h2>
    <form @submit.prevent="handleLogin" class="space-y-6">
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">Username</label>
        <input 
          v-model="form.username" 
          type="text" 
          class="w-full bg-gray-50 border border-gray-300 rounded-lg p-3 text-gray-900 focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-all"
          placeholder="Enter your username"
          required
        />
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">Password</label>
        <input 
          v-model="form.password" 
          type="password" 
          class="w-full bg-gray-50 border border-gray-300 rounded-lg p-3 text-gray-900 focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-all"
          placeholder="Enter your password"
          required
        />
      </div>
      <div v-if="error" class="text-red-500 text-sm text-center bg-red-50 p-2 rounded-lg">{{ error }}</div>
      <button 
        type="submit" 
        :disabled="loading"
        class="w-full bg-primary hover:bg-purple-600 text-white font-bold py-3 px-4 rounded-lg transition-all shadow-md hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {{ loading ? 'Logging in...' : 'Sign In' }}
      </button>
      <div class="text-center mt-4">
        <router-link to="/register" class="text-gray-500 hover:text-primary text-sm font-medium transition-colors">
          Don't have an account? <span class="underline">Register</span>
        </router-link>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({
  username: '',
  password: ''
})
const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
  loading.value = true
  error.value = ''
  try {
    // Use axios directly or the configured api instance (which has base url)
    // Using configured api might be better but login is special, sometimes need specific handling
    // But here I used axios directly so I need full path or manual config if not using proxy? 
    // Proxy is set to /api, so /api/login works.
    const response = await axios.post('/api/login', new URLSearchParams({
      username: form.value.username,
      password: form.value.password
    }), {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
    
    const { access_token } = response.data
    authStore.setToken(access_token)
    authStore.setUser({ username: form.value.username })
    
    router.push('/')
  } catch (e: any) {
    error.value = e.response?.data?.detail || 'Login failed'
  } finally {
    loading.value = false
  }
}
</script>
