import { useState, useEffect } from 'react';
import { nutritionApi } from '../api/nutrition';
import { DailyNutritionSummary, FoodLog } from '../types';
import toast from 'react-hot-toast';
import { format } from 'date-fns';
import { Plus, Trash2, PieChart as PieChartIcon } from 'lucide-react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';

export default function NutritionPage() {
  const [date, setDate] = useState(format(new Date(), 'yyyy-MM-dd'));
  const [summary, setSummary] = useState<DailyNutritionSummary | null>(null);
  const [loading, setLoading] = useState(true);

  // Form state
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    foodName: '',
    mealCategory: 'BREAKFAST',
    calories: '',
    proteinG: '',
    carbsG: '',
    fatG: '',
    fiberG: '',
  });

  useEffect(() => {
    loadData();
  }, [date]);

  const loadData = async () => {
    try {
      setLoading(true);
      const res = await nutritionApi.getDailyAnalytics(date);
      if (res.success) setSummary(res.data);
    } catch (e) {
      toast.error('Failed to load nutrition data');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const payload = {
        ...formData,
        date,
        calories: parseFloat(formData.calories) || 0,
        proteinG: parseFloat(formData.proteinG) || 0,
        carbsG: parseFloat(formData.carbsG) || 0,
        fatG: parseFloat(formData.fatG) || 0,
        fiberG: parseFloat(formData.fiberG) || 0,
      };
      const res = await nutritionApi.logFood(payload);
      if (res.success) {
        toast.success('Food logged!');
        setShowForm(false);
        setFormData({ foodName: '', mealCategory: 'BREAKFAST', calories: '', proteinG: '', carbsG: '', fatG: '', fiberG: '' });
        loadData();
      }
    } catch (e) {
      toast.error('Failed to log food');
    }
  };

  const renderMealGroup = (category: string) => {
    if (!summary) return null;
    const meals = summary.meals.filter(m => m.mealCategory === category);
    if (meals.length === 0) return null;

    const cals = meals.reduce((sum, m) => sum + m.calories, 0);

    return (
      <div className="mb-6">
        <h4 className="font-semibold mb-3 flex justify-between border-b border-border-color pb-2">
          {category} <span className="text-blue-400">{cals.toFixed(0)} kcal</span>
        </h4>
        <div className="flex flex-col gap-2">
          {meals.map(m => (
            <div key={m.id} className="flex justify-between items-center p-3 bg-secondary rounded-lg">
              <div>
                <p className="font-medium">{m.foodName}</p>
                <p className="text-xs text-secondary">
                  {m.proteinG}g P • {m.carbsG}g C • {m.fatG}g F
                </p>
              </div>
              <span className="font-semibold">{m.calories} kcal</span>
            </div>
          ))}
        </div>
      </div>
    );
  };

  const macroData = summary ? [
    { name: 'Protein', value: summary.totalProtein * 4, color: '#10b981' },
    { name: 'Carbs', value: summary.totalCarbs * 4, color: '#f59e0b' },
    { name: 'Fat', value: summary.totalFat * 9, color: '#8b5cf6' },
  ] : [];

  return (
    <div className="flex flex-col gap-6">
      <div className="flex justify-between items-center">
        <input 
          type="date" 
          className="input w-48 bg-card"
          value={date}
          onChange={e => setDate(e.target.value)}
        />
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
          <Plus size={18} /> Add Food
        </button>
      </div>

      {showForm && (
        <div className="card-glass animate-fade-in">
          <h3 className="font-semibold mb-4">Log Food</h3>
          <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="form-group mb-0">
              <label className="label">Food Name</label>
              <input type="text" className="input" required value={formData.foodName} onChange={e => setFormData({...formData, foodName: e.target.value})} />
            </div>
            <div className="form-group mb-0">
              <label className="label">Meal</label>
              <select className="input" value={formData.mealCategory} onChange={e => setFormData({...formData, mealCategory: e.target.value})}>
                <option value="BREAKFAST">Breakfast</option>
                <option value="LUNCH">Lunch</option>
                <option value="DINNER">Dinner</option>
                <option value="SNACK">Snack</option>
              </select>
            </div>
            <div className="form-group mb-0">
              <label className="label">Calories</label>
              <input type="number" className="input" required value={formData.calories} onChange={e => setFormData({...formData, calories: e.target.value})} />
            </div>
            <div className="form-group mb-0">
              <label className="label">Protein (g)</label>
              <input type="number" className="input" value={formData.proteinG} onChange={e => setFormData({...formData, proteinG: e.target.value})} />
            </div>
            <div className="form-group mb-0">
              <label className="label">Carbs (g)</label>
              <input type="number" className="input" value={formData.carbsG} onChange={e => setFormData({...formData, carbsG: e.target.value})} />
            </div>
            <div className="form-group mb-0">
              <label className="label">Fat (g)</label>
              <input type="number" className="input" value={formData.fatG} onChange={e => setFormData({...formData, fatG: e.target.value})} />
            </div>
            <div className="md:col-span-2 flex justify-end gap-2 mt-4">
              <button type="button" className="btn btn-ghost" onClick={() => setShowForm(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary">Save Log</button>
            </div>
          </form>
        </div>
      )}

      {loading ? (
        <div className="card-glass h-64 skeleton"></div>
      ) : summary ? (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="card-glass col-span-2">
            <h3 className="font-semibold mb-6 flex items-center gap-2">
              <Utensils size={18} /> Meals
            </h3>
            {summary.meals.length === 0 ? (
              <p className="text-secondary text-center py-8">No meals logged on this date.</p>
            ) : (
              <>
                {renderMealGroup('BREAKFAST')}
                {renderMealGroup('LUNCH')}
                {renderMealGroup('DINNER')}
                {renderMealGroup('SNACK')}
              </>
            )}
          </div>
          
          <div className="card-glass">
            <h3 className="font-semibold mb-4 flex items-center gap-2">
              <PieChartIcon size={18} /> Macros
            </h3>
            <div className="h-48 mb-4">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={macroData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={80}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {macroData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip 
                    contentStyle={{ backgroundColor: '#161b22', border: '1px solid #30363d', borderRadius: '8px' }}
                    itemStyle={{ color: '#f0f6fc' }}
                  />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="flex flex-col gap-3 text-sm">
               <div className="flex justify-between items-center"><span className="flex items-center gap-2"><div className="w-3 h-3 rounded-full bg-green-500"></div> Protein</span> <span>{summary.totalProtein.toFixed(0)}g</span></div>
               <div className="flex justify-between items-center"><span className="flex items-center gap-2"><div className="w-3 h-3 rounded-full bg-orange-500"></div> Carbs</span> <span>{summary.totalCarbs.toFixed(0)}g</span></div>
               <div className="flex justify-between items-center"><span className="flex items-center gap-2"><div className="w-3 h-3 rounded-full bg-purple-500"></div> Fat</span> <span>{summary.totalFat.toFixed(0)}g</span></div>
               <div className="flex justify-between items-center pt-2 border-t border-border-color font-bold text-lg"><span className="flex items-center gap-2">Total Cals</span> <span>{summary.totalCalories.toFixed(0)}</span></div>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}

// Ensure Utensils icon is imported
import { Utensils } from 'lucide-react';
