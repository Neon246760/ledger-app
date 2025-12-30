<template>
  <div>
    <h1 class="text-2xl font-bold text-gray-900 mb-6 tracking-tight">Statistics</h1>

    <!-- Date Range Selector (Default: current month) -->
    <div class="bg-white p-4 rounded-xl border border-gray-200 shadow-sm mb-6 flex items-center gap-3 flex-wrap">
      <label class="text-sm font-medium text-gray-700">Date Range</label>
      <input
        type="date"
        v-model="startDate"
        class="bg-gray-50 border border-gray-300 rounded-lg p-2 text-gray-900 text-sm focus:ring-2 focus:ring-primary focus:border-primary outline-none w-36 sm:w-auto"
      />
      <span class="text-gray-400">—</span>
      <input
        type="date"
        v-model="endDate"
        class="bg-gray-50 border border-gray-300 rounded-lg p-2 text-gray-900 text-sm focus:ring-2 focus:ring-primary focus:border-primary outline-none w-36 sm:w-auto"
      />
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
      <div class="bg-white p-4 rounded-xl border border-gray-200 h-64 shadow-sm hover:shadow-md transition-shadow">
        <h3 class="text-gray-500 text-sm font-medium uppercase tracking-wider mb-4">Expense by Category</h3>
        <div ref="pieExpenseRef" class="w-full h-[200px]"></div>
      </div>
      <div class="bg-white p-4 rounded-xl border border-gray-200 h-64 shadow-sm hover:shadow-md transition-shadow">
        <h3 class="text-gray-500 text-sm font-medium uppercase tracking-wider mb-4">Income by Category</h3>
        <div ref="pieIncomeRef" class="w-full h-[200px]"></div>
      </div>
    </div>
    <div class="grid grid-cols-1">
      <div class="bg-white p-4 rounded-xl border border-gray-200 h-[340px] shadow-sm hover:shadow-md transition-shadow">
        <h3 class="text-gray-500 text-sm font-medium uppercase tracking-wider mb-4">Daily Income vs Expense</h3>
        <div ref="barChartRef" class="w-full h-[240px]"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import api from '@/api'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'

const pieExpenseRef = ref<HTMLElement | null>(null)
const pieIncomeRef = ref<HTMLElement | null>(null)
const barChartRef = ref<HTMLElement | null>(null)
let pieExpenseChart: echarts.ECharts | null = null
let pieIncomeChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null

dayjs.extend(utc)
dayjs.extend(timezone)
dayjs.tz.setDefault('Asia/Shanghai')

const startDate = ref(dayjs().startOf('month').format('YYYY-MM-DD'))
const endDate = ref(dayjs().endOf('month').format('YYYY-MM-DD'))

const fetchData = async () => {
  const start = dayjs(startDate.value).startOf('day').format('YYYY-MM-DDTHH:mm:ss')
  const end = dayjs(endDate.value).endOf('day').format('YYYY-MM-DDTHH:mm:ss')
  const params = { start_date: start, end_date: end, limit: 1000 }
  const res = await api.get('/transactions', { params })
  const items = res.data.items || []

  const expenseTotals: Record<string, number> = {}
  const incomeTotals: Record<string, number> = {}
  const startDay = dayjs(startDate.value).startOf('day')
  const endDay = dayjs(endDate.value).endOf('day')
  const totalDays = Math.max(1, endDay.diff(startDay, 'day') + 1)
  const xDays = Array.from({ length: totalDays }, (_, i) =>
    startDay.add(i, 'day').format('MM-DD')
  )
  const incomeDaily = new Array(totalDays).fill(0)
  const expenseDaily = new Array(totalDays).fill(0)

  const parseToShanghai = (input: string) => {
    const hasTZ = /Z$|[\+\-]\d{2}:\d{2}$/.test(input)
    const base = hasTZ ? dayjs(input) : dayjs.utc(input)
    return base.tz('Asia/Shanghai')
  }

  for (const t of items) {
    const d = parseToShanghai(t.date)
    const dayIndex = Math.floor(d.startOf('day').diff(startDay, 'day'))
    if (dayIndex < 0 || dayIndex >= totalDays) continue
    const amount = Number(t.amount) || 0
    if (t.type === 'income') {
      incomeDaily[dayIndex] += amount
      const cat = t.category || '未分类'
      incomeTotals[cat] = (incomeTotals[cat] || 0) + amount
    } else if (t.type === 'expense') {
      expenseDaily[dayIndex] += amount
      const cat = t.category || '未分类'
      expenseTotals[cat] = (expenseTotals[cat] || 0) + amount
    }
  }

  const pieExpenseData = Object.entries(expenseTotals)
    .sort((a, b) => b[1] - a[1])
    .map(([name, value]) => ({ name, value }))
  const pieIncomeData = Object.entries(incomeTotals)
    .sort((a, b) => b[1] - a[1])
    .map(([name, value]) => ({ name, value }))

  updateExpensePieChart(pieExpenseData)
  updateIncomePieChart(pieIncomeData)
  updateBarChart(xDays, incomeDaily, expenseDaily)
}

const pieOption = (data: Array<{ name: string; value: number }>, el: HTMLElement) => {
  const w = el.clientWidth
  const h = el.clientHeight
  const s = Math.min(w, h)
  const outer = Math.floor(s * 0.35)
  const inner = Math.floor(outer * 0.6)
  return {
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item' },
    legend: {
      bottom: '0%',
      left: 'center',
      textStyle: { color: '#4B5563' }
    },
    series: [
      {
        name: 'Expense',
        type: 'pie',
        radius: [inner, outer],
        center: ['50%', '42%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#FFFFFF',
          borderWidth: 2
        },
        label: { show: false, position: 'center' },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
            color: '#1F2937'
          }
        },
        labelLine: { show: false },
        data: data.length ? data : [{ value: 0, name: 'No Data' }]
      }
    ]
  }
}

const updateExpensePieChart = (data: Array<{ name: string; value: number }>) => {
  if (!pieExpenseRef.value) return
  if (!pieExpenseChart) pieExpenseChart = echarts.init(pieExpenseRef.value)
  pieExpenseChart.setOption(pieOption(data, pieExpenseRef.value))
  pieExpenseChart.resize()
}

const updateIncomePieChart = (data: Array<{ name: string; value: number }>) => {
  if (!pieIncomeRef.value) return
  if (!pieIncomeChart) pieIncomeChart = echarts.init(pieIncomeRef.value)
  pieIncomeChart.setOption(pieOption(data, pieIncomeRef.value))
  pieIncomeChart.resize()
}

const updateBarChart = (xDays: string[], incomeDaily: number[], expenseDaily: number[]) => {
  if (!barChartRef.value) return
  if (!barChart) barChart = echarts.init(barChartRef.value)
  barChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: {
      data: ['Income', 'Expense'],
      textStyle: { color: '#4B5563' },
      bottom: 0
    },
    grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: xDays,
      axisLine: { lineStyle: { color: '#E5E7EB' } },
      axisLabel: { color: '#6B7280' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#F3F4F6' } },
      axisLabel: { color: '#6B7280' }
    },
    series: [
      { name: 'Income', type: 'bar', data: incomeDaily, itemStyle: { color: '#10B981' }, barGap: 0 },
      { name: 'Expense', type: 'bar', data: expenseDaily, itemStyle: { color: '#EF4444' } }
    ]
  })
}

onMounted(async () => {
  await fetchData()
  window.addEventListener('resize', () => {
    pieExpenseChart?.resize()
    pieIncomeChart?.resize()
    barChart?.resize()
  })
})

watch([startDate, endDate], async () => {
  await fetchData()
})
</script>
