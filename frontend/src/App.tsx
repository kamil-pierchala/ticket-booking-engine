import { useState, useEffect } from 'react';
import './App.css';

interface Seat {
  id: number;
  seatNumber: string;
  status: 'AVAILABLE' | 'LOCKED' | 'BOOKED';
}

interface BookingResponse {
  reservationId: number;
  seatId: number;
  status: string;
  expiresAt: string;
}

const API_BASE = 'http://localhost:8080/api/v1/bookings';
const EVENT_ID = 1;

function App() {
  const [seats, setSeats] = useState<Seat[]>([]);
  const [email, setEmail] = useState<string>('kamil@test.pl');
  const [activeReservation, setActiveReservation] = useState<BookingResponse | null>(null);
  const [feedback, setFeedback] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  const fetchSeats = async () => {
    try {
      const res = await fetch(`${API_BASE}/events/${EVENT_ID}/seats`);
      if (res.ok) {
        const data: Seat[] = await res.json();
        setSeats(data);
      }
    } catch {
      setFeedback({ text: 'Cannot connect to backend server. Make sure Spring Boot is running!', type: 'error' });
    }
  };

  useEffect(() => {
    fetchSeats();
    const interval = setInterval(fetchSeats, 3000);
    return () => clearInterval(interval);
  }, []);

  const handleReserve = async (seatId: number) => {
    if (!email.trim()) {
      setFeedback({ text: 'Please enter a valid email address first.', type: 'error' });
      return;
    }

    try {
      const res = await fetch(`${API_BASE}/reserve`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ seatId, userEmail: email })
      });

      if (res.ok) {
        const data: BookingResponse = await res.json();
        setActiveReservation(data);
        setFeedback({ text: `Seat reserved! Complete payment within 10 minutes.`, type: 'success' });
        fetchSeats();
      } else {
        const err = await res.json();
        setFeedback({ text: err.message || 'Seat could not be reserved.', type: 'error' });
        fetchSeats();
      }
    } catch {
      setFeedback({ text: 'Network error while attempting reservation.', type: 'error' });
    }
  };

  const handleConfirmPayment = async () => {
    if (!activeReservation) return;

    try {
      const res = await fetch(`${API_BASE}/${activeReservation.reservationId}/confirm-payment`, {
        method: 'POST'
      });

      if (res.ok) {
        setFeedback({ text: 'Payment successful! Ticket confirmed.', type: 'success' });
        setActiveReservation(null);
        fetchSeats();
      } else {
        setFeedback({ text: 'Payment failed or reservation expired.', type: 'error' });
      }
    } catch {
      setFeedback({ text: 'Network error while confirming payment.', type: 'error' });
    }
  };

  return (
    <div>
      <h1 className="title">Ticket Booking Engine</h1>
      <p className="subtitle">Interactive Full-Stack Concurrency Demo</p>

      <div className="card">
        <label style={{ marginRight: '1rem', fontWeight: 600 }}>Your Email:</label>
        <input
          type="email"
          className="email-input"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="your.email@domain.com"
        />
      </div>

      <div className="screen-container">
        <div className="screen">STAGE / SCREEN</div>
      </div>

      <div className="seats-grid">
        {seats.map((seat) => (
          <button
            key={seat.id}
            className={`seat-btn seat-${seat.status}`}
            disabled={seat.status !== 'AVAILABLE'}
            onClick={() => handleReserve(seat.id)}
          >
            {seat.seatNumber}
          </button>
        ))}
      </div>

      <div className="legend">
        <div className="legend-item"><div className="dot seat-AVAILABLE"></div> Available</div>
        <div className="legend-item"><div className="dot seat-LOCKED"></div> Locked (Hold)</div>
        <div className="legend-item"><div className="dot seat-BOOKED"></div> Booked</div>
      </div>

      {feedback && (
        <div className={`message-box msg-${feedback.type}`}>
          {feedback.text}
        </div>
      )}

      {activeReservation && (
        <div className="panel-confirm">
          <h3>Pending Reservation #{activeReservation.reservationId}</h3>
          <p>Status: <strong>{activeReservation.status}</strong></p>
          <p>Seat ID: <strong>{activeReservation.seatId}</strong></p>
          <button className="btn-pay" onClick={handleConfirmPayment}>
            Confirm & Pay Ticket
          </button>
        </div>
      )}
    </div>
  );
}

export default App;