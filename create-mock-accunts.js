const backend_uri = "http://localhost:8080";

const students = [
  {'username': 'ramirezangela', 'password': 'cqw1QUYaM_', 'firstname': 'Alicia', 'lastname': 'Price'},
  {'username': 'andrewwilliams', 'password': 'vpW*_$5n^0', 'firstname': 'Micheal', 'lastname': 'Page'},
  {'username': 'michael97', 'password': 'Pr07PRvr&v', 'firstname': 'Charles', 'lastname': 'Powers'},
  {'username': 'bridgetrios', 'password': '5&3C6h+Vyq', 'firstname': 'Richard', 'lastname': 'Hammond'},
  {'username': 'fwheeler', 'password': '#jNpOhG1I9', 'firstname': 'Zachary', 'lastname': 'Williams'},
];

const teachers = [
  {'username': 'charlesangelica', 'password': '+!1aY3C%Cp', 'firstname': 'Matthew', 'lastname': 'Davis', 'role': 'ROLE_TEACHER'},
  {'username': 'austinowens', 'password': '&6dv1SPvAd', 'firstname': 'Dakota', 'lastname': 'Wilson', 'role': 'ROLE_TEACHER'},
];

const courses = [
  {
    'name': 'Principles of Microeconomics',
    'description': 'Introduction to basic economic concepts such as supply and demand, '
      +'market structures, and consumer behavior. '
      + 'Focus on real-world applications and current economic issues.'
  },
  {
    'name': 'Data Structures and Algorithms',
    'description': 'A study of fundamental data structures (lists, stacks, queues, trees, graphs) '
      +'and algorithms for sorting, searching, and traversal. '
      +'Emphasis on time and space complexity analysis.'
  }
];

(async () => {
  const session = await fetch(
    `${backend_uri}/signin`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        username: 'admin',
        password: 'administrator'
      })
    }
  ).then(resp => resp.json());

  console.log(session);

  const httpRequest = async (url, method, body, token) => {
    const headers = new Headers({
      'Content-Type': 'application/json'
    });

    if(token) {
      headers.append('Authorization', `Bearer ${token}`);
    }

    const response = await fetch(url, {
      method: method,
      headers: headers,
      body: body ? JSON.stringify(body) : undefined
    })

    const response_type = response.status / 100;
    switch(response_type) {
      case 4:
      case 5:
        console.error(await response.json());
        throw new Error(`request failed with status ${response.status}`);
        
      default:
        return response;
    }
  };

  const adminHttpRequest = async (url, method, body) => {
    return httpRequest(url,method, body, session.token);
  }

  teachers.forEach(async (teacher, idx) => {
    const teacherToken = await adminHttpRequest(`${backend_uri}/teacher-token`, 'POST', {
      reusable: false
    }).then(resp => resp.json())

    await httpRequest(`${backend_uri}/signup`, 'POST', {
      ...teacher,
      teacherToken: teacherToken.token
    })

    const teacherSession = await httpRequest(`${backend_uri}/signin`, 'POST', {
      username: teacher.username,
      password: teacher.password,
    }).then(resp => resp.json());

    await httpRequest(`${backend_uri}/subject`, 'POST', courses[idx], teacherSession.token);
  });

  students.forEach(async (student) => {
    await httpRequest(`${backend_uri}/signup`, 'POST', student);
  });
  
})()
