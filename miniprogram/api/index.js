// 小程序 API 封装（复用后端 /api，需配 app.json 的 request 合法域名）
const BASE = 'http://localhost:8080'; // 开发联调；上线改为后端正式域名

function request(method, path, data, token) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE + path,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      success: (res) => {
        // 后端统一 Result { code, message, data }
        if (res.data && res.data.code === 200) resolve(res.data.data);
        else reject(res.data || res);
      },
      fail: reject
    });
  });
}

export const getMe = () => request('GET', '/api/auth/me', null, getToken());
export const getMyEnrollment = (studentId) =>
  request('GET', `/api/dorm/enrollment/list?studentId=${studentId}&current=1&size=10`, null, getToken());
export const getMyBills = (roomId) =>
  request('GET', `/api/dorm/bill/list?roomId=${roomId}&current=1&size=20`, null, getToken());
export const payBill = (body) => request('POST', '/api/dorm/payment', body, getToken());
export const submitRepair = (body) => request('POST', '/api/dorm/repair/submit', body, getToken());
export const getMyRepairs = () =>
  request('GET', '/api/dorm/repair/list?current=1&size=20', null, getToken());

// 卫生检查（宿管扫码）
export const getPlanList = () =>
  request('GET', '/api/maint/check/plan/list', null, getToken());
export const getRoomByNo = (roomId) =>
  request('GET', `/api/dorm/room/${roomId}`, null, getToken());
export const submitCheck = (body) =>
  request('POST', '/api/maint/check/record/submit', body, getToken());
export const getRoomScore = (roomId, period) =>
  request('GET', `/api/maint/check/record/room-score?roomId=${roomId}&period=${period}`, null, getToken());

// 评优查询（学生视角）
export const calcScore = (roomId, period) =>
  request('GET', `/api/maint/eval/score/calculate?roomId=${roomId}&period=${period}`, null, getToken());

export function getToken() {
  return uni.getStorageSync('token') || '';
}
export function setToken(t) {
  uni.setStorageSync('token', t);
}
