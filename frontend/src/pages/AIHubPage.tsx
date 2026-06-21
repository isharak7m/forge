import { useState, useEffect } from 'react';
import { aiApi } from '../api/ai';
import { WeightPrediction, PlateauAlert, AdherenceScore, Recommendation, AssistantResponse } from '../types';
import { Brain, TrendingUp, AlertTriangle, Target, Send, Activity, Info } from 'lucide-react';
import toast from 'react-hot-toast';

export default function AIHubPage() {
  const [prediction, setPrediction] = useState<WeightPrediction | null>(null);
  const [plateaus, setPlateaus] = useState<PlateauAlert[]>([]);
  const [adherence, setAdherence] = useState<AdherenceScore | null>(null);
  const [recs, setRecs] = useState<Recommendation[]>([]);
  
  const [query, setQuery] = useState('');
  const [chatLog, setChatLog] = useState<{role: 'user'|'ai', content: string, sources?: string[]}[]>([]);
  const [isTyping, setIsTyping] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => { document.title = 'AI Hub - FitMind'; }, []);

  useEffect(() => {
    loadAIData();
  }, []);

  const loadAIData = async () => {
    try {
      setLoading(true);
      const [predRes, platRes, adhRes, recRes] = await Promise.all([
        aiApi.getPrediction(),
        aiApi.getPlateaus(),
        aiApi.getAdherence(),
        aiApi.getRecommendations()
      ]);
      if (predRes.success) setPrediction(predRes.data);
      if (platRes.success) setPlateaus(platRes.data);
      if (adhRes.success) setAdherence(adhRes.data);
      if (recRes.success) setRecs(recRes.data);
      
      setChatLog([{
        role: 'ai',
        content: "Hello! I'm FitMind AI. I've analyzed your recent data. Ask me about your trends, PRs, or what you should do next!"
      }]);
    } catch (e) {
      toast.error('Failed to load AI models');
    } finally {
      setLoading(false);
    }
  };

  const handleChat = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!query.trim()) return;

    const userMsg = query;
    setChatLog(prev => [...prev, { role: 'user', content: userMsg }]);
    setQuery('');
    setIsTyping(true);

    const apiKey = import.meta.env.VITE_GROK_API_KEY as string;
    if (!apiKey) {
      setChatLog(prev => [...prev, { role: 'ai', content: 'AI assistant is not configured (VITE_GROK_API_KEY missing). Please set it in your .env file.' }]);
      setIsTyping(false);
      return;
    }

    try {
      const systemMsg = 'You are FitMind AI, a knowledgeable fitness assistant. Answer questions about fitness, nutrition, workouts, and health. Be concise and practical.';
      const messages = [
        { role: 'system', content: systemMsg },
        ...chatLog.filter(m => m.role === 'user' || m.role === 'ai').map(m => ({ role: m.role, content: m.content })),
        { role: 'user', content: userMsg }
      ];
      const res = await fetch('https://api.x.ai/v1/chat/completions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${apiKey}` },
        body: JSON.stringify({ model: 'grok-2-latest', messages, stream: true })
      });
      if (!res.ok || !res.body) throw new Error('API error');
      setChatLog(prev => [...prev, { role: 'ai', content: '' }]);
      const reader = res.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';
        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const data = line.slice(6).trim();
            if (data === '[DONE]') continue;
            try {
              const parsed = JSON.parse(data);
              const delta = parsed.choices?.[0]?.delta?.content || '';
              if (delta) {
                setChatLog(prev => {
                  const copy = [...prev];
                  const last = { ...copy[copy.length - 1], content: copy[copy.length - 1].content + delta };
                  copy[copy.length - 1] = last;
                  return copy;
                });
              }
            } catch {}
          }
        }
      }
    } catch (e) {
      toast.error('AI Assistant error');
    } finally {
      setIsTyping(false);
    }
  };

  if (loading) return <div className="text-center p-12"><Brain className="animate-pulse mx-auto text-blue-500 mb-4" size={48} /> Loading Neural Models...</div>;

  return (
    <div className="flex flex-col gap-6">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        
        {/* Prediction Card */}
        <div className="card-glass">
          <div className="flex items-center gap-2 mb-4 text-blue-400">
            <TrendingUp /> <h3 className="font-semibold text-white">Weight Forecast</h3>
          </div>
          {prediction && (
            <div>
              <div className="flex items-end gap-2 mb-6">
                <span className="text-4xl font-bold">{prediction.predicted30Days}</span>
                <span className="text-secondary pb-1">kg in 30 days</span>
              </div>
              <div className="flex gap-2 mb-4">
                <span className={`badge ${prediction.trend === 'LOSING' ? 'badge-green' : prediction.trend === 'GAINING' ? 'badge-blue' : 'badge-orange'}`}>
                  Trend: {prediction.trend}
                </span>
                <span className="badge badge-purple">Conf: {prediction.confidence}</span>
              </div>
              <p className="text-sm text-secondary mb-2"><Info size={14} className="inline mr-1"/> {prediction.methodology}</p>
              <ul className="text-sm text-muted list-disc pl-4">
                {prediction.keyFactors.map((f, i) => <li key={i}>{f}</li>)}
              </ul>
            </div>
          )}
        </div>

        {/* Plateaus */}
        <div className="card-glass">
          <div className="flex items-center gap-2 mb-4 text-orange-400">
            <AlertTriangle /> <h3 className="font-semibold text-white">Stagnation Alerts</h3>
          </div>
          {plateaus.length === 0 ? (
            <div className="text-center py-8 text-green-400 border border-green-400/20 rounded-lg bg-green-400/5">
              No plateaus detected! Keep it up.
            </div>
          ) : (
            <div className="flex flex-col gap-3">
              {plateaus.map((p, i) => (
                <div key={i} className="p-3 bg-[rgba(245,158,11,0.1)] border border-[rgba(245,158,11,0.2)] rounded-lg">
                  <div className="flex justify-between items-start mb-1">
                    <span className="text-sm font-semibold text-orange-400">{p.affectedMetric}</span>
                    <span className="text-xs badge badge-red">{p.daysStagnant} days</span>
                  </div>
                  <p className="text-sm text-secondary">{p.description}</p>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Adherence */}
        <div className="card-glass">
          <div className="flex items-center gap-2 mb-4 text-green-400">
            <Target /> <h3 className="font-semibold text-white">Adherence Score</h3>
          </div>
          {adherence && (
            <div className="flex flex-col items-center">
              <div className="relative w-32 h-32 mb-4 flex items-center justify-center rounded-full border-4 border-secondary">
                 {/* simplified circle */}
                 <div className="absolute inset-0 rounded-full border-4 border-green-500 border-t-transparent" style={{transform: `rotate(${adherence.overallScore * 3.6}deg)`}}></div>
                 <div className="text-3xl font-bold">{adherence.overallScore}</div>
              </div>
              <div className="w-full text-sm">
                <div className="flex justify-between mb-1"><span>Workout</span> <span className="text-blue-400">{adherence.workoutConsistency}/40</span></div>
                <div className="flex justify-between mb-1"><span>Nutrition</span> <span className="text-green-400">{adherence.nutritionConsistency}/30</span></div>
                <div className="flex justify-between mb-1"><span>Sleep</span> <span className="text-purple-400">{adherence.sleepConsistency}/20</span></div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Recommendations */}
      <div className="card-glass">
        <div className="flex items-center gap-2 mb-4">
          <Activity className="text-purple-400"/> <h3 className="font-semibold text-lg">AI Action Plan</h3>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {recs.map((r, i) => (
            <div key={i} className="p-4 bg-secondary rounded-lg border border-border-color hover:border-purple-500/30 transition-colors">
              <div className="flex justify-between mb-2">
                <h4 className="font-semibold text-primary">{r.title}</h4>
                <span className={`badge ${r.priority === 'HIGH' ? 'badge-red' : 'badge-blue'}`}>{r.priority}</span>
              </div>
              <p className="text-sm text-secondary mb-3">{r.reason}</p>
              <div className="text-sm font-medium text-purple-400 bg-purple-500/10 px-3 py-2 rounded">
                Action: {r.actionItem}
              </div>
            </div>
          ))}
          {recs.length === 0 && <p className="text-secondary">No specific actions required currently. You're doing great!</p>}
        </div>
      </div>

      {/* Chat */}
      <div className="card-glass flex flex-col h-[500px]">
        <h3 className="font-semibold mb-4 border-b border-border-color pb-4 flex items-center gap-2">
          <Brain className="text-blue-400"/> Fitness Assistant
        </h3>
        
        <div className="flex-1 overflow-y-auto mb-4 flex flex-col gap-4 pr-2">
          {chatLog.map((msg, i) => (
            <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
              <div className={`max-w-[80%] p-3 rounded-2xl ${msg.role === 'user' ? 'bg-blue-600 text-white rounded-tr-sm' : 'bg-secondary text-primary rounded-tl-sm border border-border-color'}`}>
                <p className="text-sm leading-relaxed">{msg.content}</p>
                {msg.sources && msg.sources.length > 0 && (
                  <div className="mt-2 pt-2 border-t border-[rgba(255,255,255,0.1)] flex flex-wrap gap-1">
                    {msg.sources.map((s, si) => <span key={si} className="text-[10px] bg-[rgba(0,0,0,0.2)] px-1.5 py-0.5 rounded text-gray-300">{s}</span>)}
                  </div>
                )}
              </div>
            </div>
          ))}
          {isTyping && (
             <div className="flex justify-start">
               <div className="bg-secondary p-3 rounded-2xl rounded-tl-sm border border-border-color flex gap-1">
                 <div className="w-2 h-2 rounded-full bg-blue-400 animate-bounce"></div>
                 <div className="w-2 h-2 rounded-full bg-blue-400 animate-bounce" style={{animationDelay: '0.2s'}}></div>
                 <div className="w-2 h-2 rounded-full bg-blue-400 animate-bounce" style={{animationDelay: '0.4s'}}></div>
               </div>
             </div>
          )}
        </div>

        <form onSubmit={handleChat} className="relative mt-auto">
          <input 
            type="text" 
            value={query}
            onChange={e => setQuery(e.target.value)}
            className="input pr-12 bg-secondary border-border-color focus:border-blue-500 rounded-full"
            placeholder="Ask about your calories, predictions, or PRs..."
          />
          <button type="submit" disabled={!query.trim() || isTyping} className="absolute right-2 top-1/2 -translate-y-1/2 p-2 text-blue-400 hover:text-blue-300 disabled:opacity-50">
            <Send size={18} />
          </button>
        </form>
        <div className="flex gap-2 mt-3 overflow-x-auto pb-1">
          {["What is my predicted weight?", "How are my calories this month?", "What's my best bench press?"].map((s, i) => (
             <button key={i} type="button" onClick={() => setQuery(s)} className="text-xs whitespace-nowrap bg-secondary hover:bg-[rgba(255,255,255,0.1)] px-3 py-1.5 rounded-full text-secondary transition-colors border border-border-color">
               {s}
             </button>
          ))}
        </div>
      </div>
    </div>
  );
}
