import { useState, useEffect } from 'react';
import { nutritionApi } from '../api/nutrition';
import { DailyNutritionSummary, FoodLog } from '../types';
import toast from 'react-hot-toast';
import { format } from 'date-fns';
import { Plus, Trash2, Utensils, PieChart as PieChartIcon } from 'lucide-react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';

export default function NutritionPage() {
  const [date, setDate] = useState(format(new Date(), 'yyyy-MM-dd'));
  const [summary, setSummary] = useState<DailyNutritionSummary | null>(null);
  const [loading, setLoading] = useState(true);

  // Form state
  const [showForm, setShowForm] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [searching, setSearching] = useState(false);
  const [servingG, setServingG] = useState(100);
  const [basePer100g, setBasePer100g] = useState<Record<string, number>>({});
  const [formData, setFormData] = useState({
    foodName: '',
    mealCategory: 'BREAKFAST',
    calories: '',
    proteinG: '',
    carbsG: '',
    fatG: '',
    fiberG: '',
  });

  const applyServingScale = (base: Record<string, number>, serving: number) => {
    const ratio = serving / 100;
    setFormData(prev => ({
      ...prev,
      calories: String(Math.round((base.calories || 0) * ratio)),
      proteinG: String(Math.round((base.proteinG || 0) * ratio)),
      carbsG: String(Math.round((base.carbsG || 0) * ratio)),
      fatG: String(Math.round((base.fatG || 0) * ratio)),
      fiberG: String(Math.round((base.fiberG || 0) * ratio)),
    }));
  };

  const handleSearch = async (query: string) => {
    setSearchQuery(query);
    if (query.length < 3) { setSearchResults([]); return; }
    setSearching(true);
    try {
      const res = await fetch(`https://world.openfoodfacts.org/cgi/search.pl?search_terms=${encodeURIComponent(query)}&search_simple=1&action=process&json=1&page_size=8&fields=product_name,brands,nutriments`);
      const data = await res.json();
      setSearchResults(data.products || []);
    } catch { setSearchResults([]); }
    finally { setSearching(false); }
  };

  const selectFood = (product: any) => {
    const n = product.nutriments || {};
    const kcal = n['energy-kcal_100g'] ?? Math.round((n['energy_100g'] || 0) / 4.184);
    const base = {
      calories: kcal,
      proteinG: Math.round(n.proteins_100g || 0),
      carbsG: Math.round(n.carbohydrates_100g || 0),
      fatG: Math.round(n.fat_100g || 0),
      fiberG: Math.round(n.fiber_100g || 0),
    };
    setBasePer100g(base);
    setServingG(100);
    applyServingScale(base, 100);
    setFormData(prev => ({
      ...prev,
      foodName: product.product_name || product.product_name_en || searchQuery,
    }));
    setSearchQuery('');
    setSearchResults([]);
  };

  useEffect(() => { document.title = 'Nutrition - FitMind'; }, []);

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
    const payload = {
      ...formData,
      date,
      calories: parseFloat(formData.calories) || 0,
      proteinG: parseFloat(formData.proteinG) || 0,
      carbsG: parseFloat(formData.carbsG) || 0,
      fatG: parseFloat(formData.fatG) || 0,
      fiberG: parseFloat(formData.fiberG) || 0,
    };
    const optimistic: FoodLog = {
      id: -Date.now(),
      date,
      mealCategory: payload.mealCategory,
      foodName: payload.foodName,
      servingSize: 1,
      unit: 'serving',
      calories: payload.calories,
      proteinG: payload.proteinG,
      carbsG: payload.carbsG,
      fatG: payload.fatG,
      fiberG: payload.fiberG,
      loggedAt: new Date().toISOString(),
    };
    setSummary(prev => prev ? { ...prev, meals: [...prev.meals, optimistic], totalCalories: prev.totalCalories + optimistic.calories, totalProtein: prev.totalProtein + optimistic.proteinG, totalCarbs: prev.totalCarbs + optimistic.carbsG, totalFat: prev.totalFat + optimistic.fatG } : prev);
    setShowForm(false);
    setFormData({ foodName: '', mealCategory: 'BREAKFAST', calories: '', proteinG: '', carbsG: '', fatG: '', fiberG: '' });
    try {
      const res = await nutritionApi.logFood(payload);
      if (res.success) {
        toast.success('Food logged!');
      }
      loadData();
    } catch (e) {
      toast.error('Failed to log food');
      loadData();
    }
  };

  const handleDeleteLog = async (id: number) => {
    if (!window.confirm('Delete this food log entry?')) return;
    setSummary(prev => prev ? { ...prev, meals: prev.meals.filter(m => m.id !== id) } : prev);
    try {
      await nutritionApi.deleteLog(id);
      toast.success('Food log deleted');
      loadData();
    } catch (e) {
      toast.error('Failed to delete food log');
      loadData();
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
              <div className="flex items-center gap-3">
                <span className="font-semibold">{m.calories} kcal</span>
                <button onClick={() => handleDeleteLog(m.id)} className="text-red-400 hover:text-red-300 transition-colors">
                  <Trash2 size={16} />
                </button>
              </div>
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
            <div style={{ position: 'relative' }} className="form-group mb-0" onBlur={() => setTimeout(() => setSearchResults([]), 200)}>
              <label className="label">Search Food (Open Food Facts)</label>
              <input type="text" className="input" placeholder="e.g., chicken breast..." value={searchQuery} onChange={e => handleSearch(e.target.value)} />
              {searching && <p className="text-xs text-secondary mt-1">Searching...</p>}
              {searchResults.length > 0 && (
                <div style={{ position: 'absolute', top: '100%', left: 0, right: 0, zIndex: 50 }} className="bg-[#1c2128] border border-[rgba(48,54,61,0.8)] rounded-lg mt-1 max-h-48 overflow-y-auto shadow-xl">
                  {searchResults.map((p, i) => {
                    const n = p.nutriments || {};
                    const kcal = n['energy-kcal_100g'] ?? Math.round((n['energy_100g'] || 0) / 4.184);
                    return (
                      <button key={i} type="button" className="w-full text-left px-3 py-2 text-sm hover:bg-[rgba(59,130,246,0.1)] transition-colors border-b border-[rgba(48,54,61,0.5)] last:border-b-0" onMouseDown={() => selectFood(p)}>
                        <span className="font-medium">{p.product_name || p.product_name_en || 'Unknown'}</span>
                        <span className="text-secondary ml-2">{kcal} kcal/100g</span>
                      </button>
                    );
                  })}
                </div>
              )}
            </div>
            <div className="form-group mb-0">
              <label className="label">Serving Size (g)</label>
              <input type="number" className="input" min={1} value={servingG} onChange={e => { const v = parseInt(e.target.value) || 100; setServingG(v); if (Object.keys(basePer100g).length) applyServingScale(basePer100g, v); }} />
            </div>
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

