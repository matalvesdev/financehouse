import { Link } from 'react-router-dom'
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Line,
  Area,
  AreaChart,
  RadialBarChart,
  RadialBar,
  ComposedChart,
} from 'recharts'

// Componente de tooltip customizado para gráficos
const CustomTooltip = ({ active, payload, label }: any) => {
  if (active && payload && payload.length) {
    return (
      <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg">
        <p className="font-semibold text-gray-900 mb-2">{label}</p>
        {payload.map((entry: any, index: number) => (
          <p key={index} className="text-sm" style={{ color: entry.color }}>
            {entry.name}: R$ {entry.value.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
          </p>
        ))}
      </div>
    )
  }
  return null
}

// Componente de tooltip para gráfico de pizza
const PieTooltip = ({ active, payload }: any) => {
  if (active && payload && payload.length) {
    const data = payload[0]
    return (
      <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg">
        <p className="font-semibold text-gray-900">{data.name}</p>
        <p className="text-sm text-gray-600">
          R$ {data.value.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
        </p>
        <p className="text-xs text-gray-500">
          {((data.value / data.payload.total) * 100).toFixed(1)}% do total
        </p>
      </div>
    )
  }
  return null
}

interface IncomeExpenseChartProps {
  receitaMensal: number
  despesaMensal: number
  saldoMensal: number
}

export function IncomeExpenseChart({ receitaMensal, despesaMensal, saldoMensal }: IncomeExpenseChartProps) {
  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">
        Receitas vs Despesas (Mês Atual)
      </h3>
      <div className="h-64">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart
            data={[
              {
                name: 'Resumo Mensal',
                Receitas: receitaMensal,
                Despesas: despesaMensal,
              },
            ]}
            margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis dataKey="name" stroke="#6B7280" />
            <YAxis 
              stroke="#6B7280"
              tickFormatter={(value) => `R$${(value / 1000).toFixed(0)}k`}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend />
            <Bar dataKey="Receitas" fill="#22C55E" radius={[8, 8, 0, 0]} />
            <Bar dataKey="Despesas" fill="#EF4444" radius={[8, 8, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>
      <div className="mt-4 flex justify-between items-center pt-4 border-t">
        <span className="font-semibold text-gray-900">Saldo do Mês</span>
        <span className={`font-bold text-lg ${saldoMensal >= 0 ? 'text-green-600' : 'text-red-600'}`}>
          {saldoMensal >= 0 ? '+' : ''}R$ {saldoMensal.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
        </span>
      </div>
    </div>
  )
}

interface CategoryData {
  name: string
  value: number
  color: string
  total: number
}

interface CategoryChartProps {
  categoryData: CategoryData[]
}

export function CategoryChart({ categoryData }: CategoryChartProps) {
  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">
        Despesas por Categoria
      </h3>
      {categoryData.length > 0 ? (
        <div className="h-64">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={categoryData}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={90}
                paddingAngle={2}
                dataKey="value"
                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                labelLine={false}
              >
                {categoryData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip content={<PieTooltip />} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      ) : (
        <div className="h-64 flex items-center justify-center text-gray-500">
          Nenhuma despesa registrada
        </div>
      )}
      {categoryData.length > 0 && (
        <div className="mt-4 grid grid-cols-2 gap-2">
          {categoryData.map((cat, idx) => (
            <div key={idx} className="flex items-center space-x-2 text-sm">
              <div 
                className="w-3 h-3 rounded-full" 
                style={{ backgroundColor: cat.color }}
              />
              <span className="text-gray-600 truncate">{cat.name}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

interface TrendData {
  mes: string
  Receitas: number
  Despesas: number
  Saldo: number
}

interface TrendChartProps {
  trendData: TrendData[]
}

export function TrendChart({ trendData }: TrendChartProps) {
  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">
        Tendência de Receitas e Despesas (Últimos 6 Meses)
      </h3>
      {trendData.length > 0 ? (
        <div className="h-80">
          <ResponsiveContainer width="100%" height="100%">
            <ComposedChart
              data={trendData}
              margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
            >
              <defs>
                <linearGradient id="colorReceitas" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#22C55E" stopOpacity={0.3}/>
                  <stop offset="95%" stopColor="#22C55E" stopOpacity={0}/>
                </linearGradient>
                <linearGradient id="colorDespesas" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#EF4444" stopOpacity={0.3}/>
                  <stop offset="95%" stopColor="#EF4444" stopOpacity={0}/>
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="mes" stroke="#6B7280" />
              <YAxis 
                stroke="#6B7280"
                tickFormatter={(value) => `R$${(value / 1000).toFixed(0)}k`}
              />
              <Tooltip content={<CustomTooltip />} />
              <Legend />
              <Area 
                type="monotone" 
                dataKey="Receitas" 
                fill="url(#colorReceitas)" 
                stroke="#22C55E" 
                strokeWidth={2}
              />
              <Area 
                type="monotone" 
                dataKey="Despesas" 
                fill="url(#colorDespesas)" 
                stroke="#EF4444" 
                strokeWidth={2}
              />
              <Line 
                type="monotone" 
                dataKey="Saldo" 
                stroke="#3B82F6" 
                strokeWidth={3}
                dot={{ fill: '#3B82F6', r: 4 }}
              />
            </ComposedChart>
          </ResponsiveContainer>
        </div>
      ) : (
        <div className="h-80 flex items-center justify-center text-gray-500">
          Dados insuficientes para exibir tendência
        </div>
      )}
    </div>
  )
}

interface BudgetChartData {
  categoria: string
  Gasto: number
  Limite: number
  Disponível: number
  percentual: number
}

interface BudgetChartProps {
  budgetChartData: BudgetChartData[]
}

export function BudgetProgressChart({ budgetChartData }: BudgetChartProps) {
  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border">
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-lg font-semibold text-gray-900">
          Progresso dos Orçamentos
        </h3>
        <Link to="/budgets" className="text-blue-600 hover:text-blue-800 text-sm">
          Ver todos
        </Link>
      </div>
      {budgetChartData.length > 0 ? (
        <div className="h-80">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart
              data={budgetChartData}
              layout="vertical"
              margin={{ top: 5, right: 30, left: 100, bottom: 5 }}
            >
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis 
                type="number" 
                stroke="#6B7280"
                tickFormatter={(value) => `R$${(value / 1000).toFixed(0)}k`}
              />
              <YAxis 
                type="category" 
                dataKey="categoria" 
                stroke="#6B7280"
                width={90}
              />
              <Tooltip content={<CustomTooltip />} />
              <Legend />
              <Bar 
                dataKey="Gasto" 
                stackId="a"
                fill="#EF4444" 
                radius={[0, 4, 4, 0]}
              />
              <Bar 
                dataKey="Disponível" 
                stackId="a"
                fill="#22C55E" 
                radius={[0, 4, 4, 0]}
              />
            </BarChart>
          </ResponsiveContainer>
        </div>
      ) : (
        <div className="h-80 flex items-center justify-center text-gray-500">
          <div className="text-center">
            <p className="mb-2">Nenhum orçamento ativo</p>
            <Link
              to="/budgets"
              className="text-blue-600 hover:text-blue-800 text-sm"
            >
              Criar primeiro orçamento
            </Link>
          </div>
        </div>
      )}
    </div>
  )
}

interface GoalChartData {
  nome: string
  percentual: number
  valor: number
  meta: number
  fill: string
}

interface GoalChartProps {
  goalChartData: GoalChartData[]
}

export function GoalProgressChart({ goalChartData }: GoalChartProps) {
  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border">
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-lg font-semibold text-gray-900">
          Progresso das Metas
        </h3>
        <Link to="/goals" className="text-blue-600 hover:text-blue-800 text-sm">
          Ver todas
        </Link>
      </div>
      {goalChartData.length > 0 ? (
        <div className="h-80">
          <ResponsiveContainer width="100%" height="100%">
            <RadialBarChart
              cx="50%"
              cy="50%"
              innerRadius="10%"
              outerRadius="90%"
              data={goalChartData}
              startAngle={90}
              endAngle={-270}
            >
              <RadialBar
                minAngle={15}
                label={{ position: 'insideStart', fill: '#fff', fontSize: 12 }}
                background
                clockWise
                dataKey="percentual"
              />
              <Legend 
                iconSize={10}
                layout="vertical"
                verticalAlign="middle"
                align="right"
                wrapperStyle={{ fontSize: '12px' }}
              />
              <Tooltip 
                content={({ active, payload }) => {
                  if (active && payload && payload.length) {
                    const data = payload[0].payload
                    return (
                      <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg">
                        <p className="font-semibold text-gray-900">{data.nome}</p>
                        <p className="text-sm text-gray-600">
                          Progresso: {data.percentual.toFixed(1)}%
                        </p>
                        <p className="text-sm text-gray-600">
                          R$ {data.valor.toLocaleString('pt-BR', { minimumFractionDigits: 2 })} de R$ {data.meta.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                        </p>
                      </div>
                    )
                  }
                  return null
                }}
              />
            </RadialBarChart>
          </ResponsiveContainer>
        </div>
      ) : (
        <div className="h-80 flex items-center justify-center text-gray-500">
          <div className="text-center">
            <p className="mb-2">Nenhuma meta ativa</p>
            <Link
              to="/goals"
              className="text-blue-600 hover:text-blue-800 text-sm"
            >
              Criar primeira meta
            </Link>
          </div>
        </div>
      )}
    </div>
  )
}
