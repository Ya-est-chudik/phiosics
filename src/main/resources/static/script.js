const canvas = document.getElementById('opticsCanvas');
const ctx = canvas.getContext('2d');
const palette = ['#e3b836', '#ff00ff', '#bf5af2', '#00f2ff'];
let currentObjColor = palette[0];
let zoomLevel = 0.5;

const ui = {
    f: document.getElementById('f-range'),
    d: document.getElementById('d-range'),
    h: document.getElementById('h-range'),
    nf: document.getElementById('num-f'),
    nd: document.getElementById('num-d'),
    nh: document.getElementById('num-h'),
    type: document.getElementById('obj-type')
};

let zoomTarget = zoomLevel;
let animationId = null;

function updateZoomDisplay() {
    const zoomPercent = Math.round(zoomLevel * 100);
    const zoomDisplay = document.getElementById('zoom-level');
    if (zoomDisplay) {
        zoomDisplay.textContent = zoomPercent + '%';
    }
}

function smoothZoom() {
    if (animationId) cancelAnimationFrame(animationId);

    function animate() {
        const diff = zoomTarget - zoomLevel;
        if (Math.abs(diff) > 0.001) {
            zoomLevel += diff * 0.2;
            updateZoomDisplay();
            update();
            animationId = requestAnimationFrame(animate);
        } else {
            zoomLevel = zoomTarget;
            updateZoomDisplay();
            update();
            animationId = null;
        }
    }
    animate();
}

function changeZoom(factor) {
    zoomTarget *= factor;
    if (zoomTarget < 0.1) zoomTarget = 0.1;
    if (zoomTarget > 3.0) zoomTarget = 3.0;
    smoothZoom();
}

function init() {
    window.addEventListener('resize', resize);

    // Загрузка сохраненной темы
    if (localStorage.getItem('theme') === 'light') {
        document.body.classList.add('light-mode');
    }

    const canvas = document.getElementById('opticsCanvas');
    canvas.addEventListener('wheel', (e) => {
        e.preventDefault();
        const delta = e.deltaY > 0 ? 0.9 : 1.1;
        zoomTarget *= delta;
        if (zoomTarget < 0.1) zoomTarget = 0.1;
        if (zoomTarget > 5.0) zoomTarget = 5.0;
        smoothZoom();
    }, { passive: false });

    [ui.f, ui.d, ui.h].forEach(el => el.addEventListener('input', (e) => {
        const numId = 'num-' + e.target.id.split('-')[0];
        document.getElementById(numId).value = e.target.value;
        update();
    }));

    [ui.nf, ui.nd, ui.nh].forEach(el => el.addEventListener('input', (e) => {
        const rangeId = e.target.id.replace('num-', '') + '-range';
        const val = parseFloat(e.target.value) || 0;
        document.getElementById(rangeId).value = val;
        update();
    }));

    ui.type.addEventListener('change', () => {
        currentObjColor = palette[Math.floor(Math.random() * palette.length)];
        update();
    });

    updateZoomDisplay();
    resize();
}

async function update() {
    const F = parseFloat(ui.f.value);
    const d = parseFloat(ui.d.value);
    const h = parseFloat(ui.h.value);

    try {
        const response = await fetch('/api/update', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ Focus: F, ObjectDistance: d, ObjectHeight: h })
        });
        const lensModel = await response.json();

        const f_img = lensModel.ImageDistance;
        const H_img = lensModel.ImageHeight;
        const mag = lensModel.IncreaseLens;

        document.getElementById('r-f').innerText = lensModel.IsValid
            ? Math.abs(f_img).toFixed(1) + ' см'
            : '∞';
        document.getElementById('r-H').innerText = lensModel.IsValid
            ? H_img.toFixed(1) + ' см'
            : '-';
        document.getElementById('r-g').innerText = lensModel.IsValid
            ? mag.toFixed(2)
            : '-';
        document.getElementById('r-type').innerText = lensModel.ImageType || '-';

        draw(F, d, h, f_img, H_img);
    } catch (e) {
        console.error('Ошибка обновления с сервера', e);
    }
}

async function addMeasurement() {
    const F = parseFloat(ui.f.value);
    const d = parseFloat(ui.d.value);
    const h = parseFloat(ui.h.value);

    if (Math.abs(d - F) < 0.1) {
        alert('Невозможно добавить измерение при d = F');
        return;
    }

    try {
        const response = await fetch('/api/measurement/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ Focus: F, ObjectDistance: d, ObjectHeight: h })
        });

        const errorStats = await response.json();
        updateErrorDisplay(errorStats);
        await updateMeasurementsTable();

    } catch (e) {
        console.error('Ошибка при добавлении измерения', e);
    }
}

async function resetMeasurements() {
    try {
        const response = await fetch('/api/measurement/reset', {
            method: 'POST'
        });

        const errorStats = await response.json();
        updateErrorDisplay(errorStats);
        await updateMeasurementsTable();

    } catch (e) {
        console.error('Ошибка при сбросе измерений', e);
    }
}

async function updateMeasurementsTable() {
    try {
        window.location.reload();
    } catch (e) {
        console.error('Ошибка при обновлении таблицы', e);
    }
}

function updateErrorDisplay(errorStats) {
    document.getElementById('err-exp').innerText = errorStats.randomError.toFixed(2) + ' см';
    document.getElementById('err-total').innerText = errorStats.totalError.toFixed(2) + ' см';
}

function resize() {
    canvas.width = canvas.offsetWidth;
    canvas.height = canvas.offsetHeight;
    update();
}

function toggleTheme() {
    document.body.classList.toggle('light-mode');
    const isLight = document.body.classList.contains('light-mode');
    localStorage.setItem('theme', isLight ? 'light' : 'dark');
    update();
}

function draw(F, d, h, f_img, H_img) {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const isLight = document.body.classList.contains('light-mode');
    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    const safe_f = isFinite(f_img) ? Math.abs(f_img) : F * 3;
    const baseScale = (canvas.width / 1.6) / Math.max(d, safe_f, F * 2.2);
    const scale = baseScale * zoomLevel;

    // Сетка
    ctx.strokeStyle = isLight ? 'rgba(2, 86, 176, 0.3)' : 'rgba(0, 242, 255, 0.2)';
    const step = 40;

    for (let x = centerX; x < canvas.width; x += step) {
        ctx.beginPath(); ctx.moveTo(x, 0); ctx.lineTo(x, canvas.height); ctx.stroke();
    }
    for (let x = centerX - step; x > 0; x -= step) {
        ctx.beginPath(); ctx.moveTo(x, 0); ctx.lineTo(x, canvas.height); ctx.stroke();
    }
    for (let y = centerY; y < canvas.height; y += step) {
        ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(canvas.width, y); ctx.stroke();
    }
    for (let y = centerY - step; y > 0; y -= step) {
        ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(canvas.width, y); ctx.stroke();
    }

    // Оптическая ось
    ctx.strokeStyle = isLight ? 'rgba(0,0,0,0.6)' : 'rgba(255,255,255,0.4)';
    ctx.beginPath(); ctx.moveTo(0, centerY); ctx.lineTo(canvas.width, centerY); ctx.stroke();

    const points = [{ v: -2 * F, l: '2F' }, { v: -F, l: 'F' }, { v: F, l: "F'" }, { v: 2 * F, l: "2F'" }];
    points.forEach(p => {
        const x = centerX + p.v * scale;
        ctx.fillStyle = isLight ? '#000' : '#fff';
        ctx.beginPath(); ctx.arc(x, centerY, 3, 0, Math.PI * 2); ctx.fill();
        ctx.font = '24px Inter, sans-serif';
        ctx.fillText(p.l, x - 10, centerY + 25);
    });

    // Линза
    const lH = 300;
    ctx.save();
    ctx.translate(centerX, centerY);
    ctx.strokeStyle = isLight ? 'rgba(0, 0, 0, 0.3)' : 'rgba(255, 255, 255, 0.4)';
    ctx.fillStyle = isLight ? 'rgba(0, 0, 0, 0.3)' : 'rgba(255, 255, 255, 0.4)';
    ctx.beginPath(); ctx.lineWidth = 3; ctx.ellipse(0, lH + 160, 40, 8, 0, 0, Math.PI * 2); ctx.stroke(); ctx.fill();
    ctx.beginPath(); ctx.lineWidth = 3; ctx.moveTo(0, lH); ctx.lineTo(0, lH + 154); ctx.stroke();
    ctx.strokeStyle = isLight ? 'rgba(0, 0, 0, 0.3)' : 'rgba(255, 255, 255, 0.4)';
    ctx.beginPath(); ctx.lineWidth = 2.5; ctx.moveTo(0, lH); ctx.lineTo(0, lH - 700); ctx.stroke();

    ctx.shadowBlur = 20;
    ctx.shadowColor = '#00f2ff';
    ctx.strokeStyle = '#00f2ff';
    ctx.fillStyle = 'rgba(0, 242, 255, 0.2)';
    ctx.beginPath(); ctx.lineWidth = 2;
    ctx.moveTo(0, -lH);
    ctx.quadraticCurveTo(80, 0, 0, lH);
    ctx.quadraticCurveTo(-80, 0, 0, -lH);
    ctx.fill();
    ctx.stroke();
    ctx.restore();

    const objX = centerX - d * scale;
    const objY = centerY - h * scale;

    if (Math.abs(d - F) > 1) {
        const imgX = centerX + f_img * scale;
        const imgY = f_img > 0 ? centerY + (H_img * scale) : centerY - (H_img * scale);

        ctx.setLineDash([5, 5]);
        ctx.strokeStyle = isLight ? 'rgba(0, 0, 0, 0.3)' : 'rgba(255, 255, 255, 0.4)';
        ctx.beginPath(); ctx.moveTo(objX, objY); ctx.lineTo(imgX, imgY); ctx.stroke();
        ctx.beginPath(); ctx.moveTo(objX, objY); ctx.lineTo(centerX, objY); ctx.lineTo(imgX, imgY); ctx.stroke();
        ctx.setLineDash([]);

        drawBody(objX, centerY, objY, currentObjColor, ui.type.value, false);
        drawBody(imgX, centerY, imgY, currentObjColor, ui.type.value, f_img > 0);
    }
}

function drawBody(x, baseY, topY, color, type, flipped) {
    ctx.save();
    const dir = flipped ? 1 : -1;
    ctx.shadowBlur = 15;
    ctx.shadowColor = color;
    ctx.fillStyle = color;

    if (type === 'pencil') {
        const w = 10;
        ctx.fillRect(x - w / 2, baseY, w, (topY - baseY) * 0.85);
        ctx.fillStyle = '#737373';
        ctx.beginPath();
        ctx.moveTo(x - w / 2, baseY + (topY - baseY) * 0.85);
        ctx.lineTo(x + w / 2, baseY + (topY - baseY) * 0.85);
        ctx.lineTo(x, topY);
        ctx.fill();
    } else if (type === 'candle') {
        ctx.fillRect(x - 7, baseY, 14, topY - baseY);
        ctx.fillStyle = '#ff9100';
        ctx.beginPath();
        ctx.ellipse(x, topY + 8 * dir, 5, 8, 0, 0, Math.PI * 2);
        ctx.fill();
    } else if (type === 'book') {
        const w = 30;
        ctx.fillRect(x - w / 2, baseY, w, topY - baseY);
        ctx.fillStyle = 'rgba(0,0,0,0.3)';
        ctx.fillRect(x - w / 2 + 3, baseY, w - 6, topY - baseY);
    } else {
        ctx.strokeStyle = color;
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.moveTo(x, baseY);
        ctx.lineTo(x, topY);
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(x, topY);
        ctx.lineTo(x - 6, topY + 12 * -dir);
        ctx.lineTo(x + 6, topY + 12 * -dir);
        ctx.fill();
    }
    ctx.restore();
}

init();
