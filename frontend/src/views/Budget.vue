<template>
  <div>
    <h1 class="text-2xl font-bold text-gray-900 mb-6 tracking-tight">Budget Management</h1>
    
    <!-- Month Selection & Overview -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <!-- Month Selector -->
      <div class="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
        <label class="block text-sm font-medium text-gray-700 mb-2">Select Month</label>
        <input type="month" v-model="currentMonth" class="w-full bg-gray-50 border border-gray-300 rounded-lg p-2.5 text-gray-900 outline-none focus:ring-2 focus:ring-primary" />
      </div>

      <!-- Total Budget Setting -->
      <div class="bg-white p-6 rounded-xl border border-gray-200 shadow-sm md:col-span-2 flex flex-col justify-center">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-gray-500 text-sm font-medium uppercase tracking-wider">Monthly Budget Goal</h3>
          <button @click="openEditModal" class="bg-primary hover:bg-purple-600 text-white font-bold py-2.5 px-5 rounded-lg transition-all shadow-sm hover:shadow-md flex items-center gap-2">
            <EditIcon class="w-5 h-5" />
            Edit Budget
          </button>
        </div>
        <div class="flex items-end gap-2">
          <span class="text-4xl font-bold text-gray-900">¥ {{ budgetAmount.toFixed(2) }}</span>
          <span class="text-gray-500 mb-1">/ month</span>
        </div>
      </div>
    </div>

    <!-- Budget Status -->
    <div class="bg-white p-6 rounded-xl border border-gray-200 shadow-sm mb-8">
      <h2 class="text-xl font-bold text-gray-900 mb-6 tracking-tight">Budget Overview</h2>
      
      <div class="space-y-6">
        <div v-if="loading" class="text-gray-500 text-center py-4">Loading...</div>
        <div v-else>
          <div class="flex justify-between text-sm font-medium mb-2">
            <span class="text-gray-700">Total Spent</span>
            <span :class="isOverBudget ? 'text-red-600' : 'text-gray-900'">
              ¥ {{ totalExpense.toFixed(2) }} / ¥ {{ budgetAmount.toFixed(2) }}
            </span>
          </div>
          <div class="w-full bg-gray-100 rounded-full h-4 overflow-hidden shadow-inner">
            <div 
              class="h-4 rounded-full transition-all duration-500" 
              :class="progressColor"
              :style="{ width: `${progressPercentage}%` }"
            ></div>
          </div>
          <p v-if="isOverBudget" class="text-red-600 text-sm mt-3 flex items-center gap-1 font-medium">
            <AlertCircleIcon class="w-4 h-4" />
            Warning: You have exceeded your monthly budget!
          </p>
          <p v-else class="text-gray-500 text-sm mt-3">
            You have spent <span class="font-bold text-gray-700">{{ progressPercentage.toFixed(1) }}%</span> of your budget.
          </p>
        </div>
      </div>
    </div>

    <!-- Edit Modal -->
    <div v-if="showEditModal" class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div class="bg-white w-full max-w-md rounded-xl border border-gray-200 shadow-2xl p-6">
        <h2 class="text-xl font-bold text-gray-900 mb-6">Set Monthly Budget</h2>
        <form @submit.prevent="saveBudget">
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">Budget Amount</label>
            <div class="relative">
              <span class="absolute left-3 top-3 text-gray-500">¥</span>
              <input 
                v-model.number="editingAmount" 
                type="number" 
                step="100" 
                class="w-full bg-gray-50 border border-gray-300 rounded-lg p-3 pl-8 text-gray-900 focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-all" 
                required
              />
            </div>
          </div>
          <div class="flex justify-end gap-3 pt-4 border-t border-gray-100">
            <button type="button" @click="showEditModal = false" class="px-4 py-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors font-medium">Cancel</button>
            <button type="submit" class="px-4 py-2 bg-primary hover:bg-purple-600 text-white rounded-lg font-bold transition-colors shadow-sm hover:shadow">Save</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import api from '@/api'
import dayjs from 'dayjs'
import { AlertCircle as AlertCircleIcon, Edit2 as EditIcon } from 'lucide-vue-next'

const currentMonth = ref(dayjs().format('YYYY-MM'))
const budgetAmount = ref(0)
const totalExpense = ref(0)
const showEditModal = ref(false)
const editingAmount = ref(0)
const loading = ref(false)

const progressPercentage = computed(() => {
  if (budgetAmount.value === 0) return 0
  const pct = (totalExpense.value / budgetAmount.value) * 100
  return Math.min(pct, 100)
})

const isOverBudget = computed(() => {
  return budgetAmount.value > 0 && totalExpense.value > budgetAmount.value
})

const progressColor = computed(() => {
  if (isOverBudget.value) return 'bg-red-500'
  if (progressPercentage.value > 80) return 'bg-orange-500'
  return 'bg-green-500'
})

const openEditModal = () => {
  editingAmount.value = budgetAmount.value
  showEditModal.value = true
}

const fetchData = async () => {
  loading.value = true
  const monthStart = dayjs(currentMonth.value).startOf('month').format('YYYY-MM-DDTHH:mm:ss')
  const monthEnd = dayjs(currentMonth.value).endOf('month').endOf('day').format('YYYY-MM-DDTHH:mm:ss')

  try {
    // 1. Fetch Budget
    // Note: Ensure your backend supports this endpoint as defined in routers/budget.py
    const budgetRes = await api.get('/budgets', { params: { month: currentMonth.value } })
    const budgets = budgetRes.data
    // Use the first budget found (assuming general budget) or default to 0
    const generalBudget = budgets.length > 0 ? budgets[0] : null
    budgetAmount.value = generalBudget ? generalBudget.amount : 0

    // 2. Fetch Expenses
    const statsRes = await api.get('/transactions/summary/statistics', { 
      params: { start_date: monthStart, end_date: monthEnd } 
    })
    totalExpense.value = statsRes.data.total_expense
  } catch (e) {
    console.error('Failed to fetch data', e)
  } finally {
    loading.value = false
  }
}

const saveBudget = async () => {
  try {
    await api.post('/budgets', {
      amount: editingAmount.value,
      month: currentMonth.value,
      category: null // General budget
    })
    showEditModal.value = false
    fetchData()
  } catch (e) {
    console.error('Failed to save budget', e)
    alert('Failed to save budget')
  }
}

watch(currentMonth, fetchData)

onMounted(() => {
  fetchData()
})
</script>
