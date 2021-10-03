const { default: axios } = require('axios');
const express = require('express')
const app = express()
const bodyParser = require('body-parser');
const port = process.env.PORT || 3000
const { Client } = require('pg');
const url = require('url');
const {busy_hours} = require('./test');
const {getCityAndCounty} = require('./ilya');
const fs = require('fs'); 
const csv = require('csv-parser');
const moment = require('moment');
const cron = require('node-cron')
const haversine = require("haversine-distance");
const e = require('express');
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());


const client = new Client({
  connectionString: process.env.DATABASE_URL,
  ssl: {
    rejectUnauthorized: false
  }
});

client.connect();

app.get('/', (req, res) => {
    res.send({test: "test"})
})

app.get('/getHeatSquares',async (req, response) => {
  let params = url.parse(req.url, true).query

  let top = params["top"]
  let bottom = params["bottom"]
  let left = params["left"]
  let right = params["right"]
  let vaccine = params["vaccine"]
  let disease = params["disease"]
  let age = params["age"] 

  console.log(top)
  console.log(bottom)
  console.log(left)
  console.log(right)
  console.log(vaccine)
  console.log(disease)
  console.log(age)

  client.query(`select * from test where longtitude between ${bottom} and ${top} and latitude between ${left} and ${right};`,async (err, res) => {
    if (err) throw err;

    const rows = await Promise.all(res.rows.map(async (it) => {
      const places = await getPlaces2(it.latitude, it.longtitude)
      return { ...it, places: places.length }
    }))

    const { county, state } = await getCityAndCounty(right, top)
    console.log(county)
    const covidPeople = await getModelData(county, rows.map((it) => it.places))
    console.log(covidPeople)

    var alldensity = 0
    rows.forEach((it) => {
      alldensity += it.density
    })

    const newRows = rows.map((row, i) => {
        var risk1 = ((covidPeople[i] * (row.density / alldensity)) / (0.2 * row.density)) * 0.4 + (row.density / 50000) * 0.6

        console.log(`1: ${(row.density / alldensity)}`)
        console.log(`2: ${covidPeople[i] * (row.density / alldensity)}`)
        console.log(`3: ${0.2 * row.density}`)
        console.log(`4: ${(covidPeople[i] * (row.density / alldensity)) / (0.2 * row.density)}`)

        // console.log(`risk 1: ${risk1}`)

        if(Number(age) > 40) {
            risk1 *= (0.277 * (age - 40))

            if(risk1 > 1) {
              risk1 = 1
            } else if(risk1 < 0) {
            risk1 = 0
            }
          }

          if(vaccine === "true") {
            console.log("wnjwnfjewnf")
            risk1 *= 0.5
            if(risk1 > 1) {
              risk1 = 1
            } else if(risk1 < 0) {
              risk1 = 0
            }
          }

          console.log(`risk 3: ${risk1}`)

          if(disease === "true" && vaccine === "false") {
            risk1 /= 0.45
            if(risk1 > 1) {
              risk1 = 1
            } else if(risk1 < 0) {
              risk1 = 0
            }
          }

        return {...row, risk: risk1 * 100}
    })

    console.log(Math.max(...newRows.map((it) => it.risk)))

    response.send(newRows)
  });
})

const googleAPIOptions = {
  radius: '5000',
  region: 'us',
}

const getPLacesLocation = async (latitude, longtitude, placeType) => {
  const result = await axios.get(`https://maps.googleapis.com/maps/api/place/textsearch/json?query=&location=${latitude},${longtitude}&radius=${googleAPIOptions.radius}&region=${googleAPIOptions.region}&type=${placeType}&key=${process.env.GOOGLE_API_KEY}`)
  const places = await Promise.all(result.data.results.map( async place => {
    const hours = await busy_hours(place.place_id, process.env.GOOGLE_API_KEY)
    return {
      geometry: place.geometry.location,
      place_id: place.place_id,
      name: place.name,
      hours
    }
  }))

  return { places }
}

const getModelData = async (county, places) => {
  const response = await axios.get('http://ec2co-ecsel-u4qnetyi7ch9-1971977146.eu-west-1.elb.amazonaws.com:5000', {params: {places, city: county}})
  return response.data
}

const getPlaces2 = async (latitude, longtitude) => {
  const result = await axios.get(`https://maps.googleapis.com/maps/api/place/textsearch/json?query=&location=${latitude},${longtitude}&radius=500&region=${googleAPIOptions.region}&type=restaurant,cafe,church,gym,supermarket,subway_station,shopping_mall,night_club,movie_theater&key=${process.env.GOOGLE_API_KEY}`)
  const res = result.data.results.filter((it) => {
    const distance = haversine({ lat: latitude, lng: longtitude }, { lat: it.geometry.location.lat, lng: it.geometry.location.lng })
    return distance <= 500
  })
  return res;
}

const getVacinationData = async(recipCounty) => {
  const date = `${moment().subtract(1, 'd').format('YYYY-MM-DD')}T00:00:00.000`
  console.log(`https://data.cdc.gov/resource/8xkx-amqh.json?date=${date}&recip_county=${recipCounty}`)
  const result = await axios.get(`https://data.cdc.gov/resource/8xkx-amqh.json?date=${date}&recip_county=${recipCounty}`)
  console.log(result.data[0]['administered_dose1_pop_pct'])
}

const getCovidData = async() => {
  const date1 = moment().subtract(1, 'd').format('MM-DD-YYYY')
  const result1 = await axios.get(`https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/${date1}.csv`)
  const data1 = result1['data']

  const date2 = moment().subtract(15, 'd').format('MM-DD-YYYY')
  const result2 = await axios.get(`https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/${date2}.csv`)
  const data2 = result2['data']

  fs.writeFileSync('data1.csv', data1, err => {
    if (err) {
      console.error(err)
      return
    }
  })

  fs.writeFileSync('data2.csv', data2, err => {
    if (err) {
      console.error(err)
      return
    }
  })

  var query1 = `insert into covidData values`
  fs.createReadStream('data1.csv')
  .pipe(csv())
  .on('data', (data) => {
    if(data['Country_Region'] == 'US') {
      query1 = query1.concat(`(\'${escape(data['Admin2'])}\', \'${escape(data['Province_State'])}\', \'${escape(data['Country_Region'])}\', \'${escape(data['Confirmed'])}\', \'${date1}\'),`)
    }
  })
  .on('end', () => {
    client.query(query1.substring(0, query1.length - 1).concat(';'), async(err, res) => {
        console.log(err)
    });
    fs.unlinkSync('data1.csv')
  });;

  var query2 = `insert into covidData values`
  fs.createReadStream('data2.csv')
  .pipe(csv())
  .on('data', (data) => {
    if(data['Country_Region'] == 'US') {
      query2 = query2.concat(`(\'${escape(data['Admin2'])}\', \'${escape(data['Province_State'])}\', \'${escape(data['Country_Region'])}\', \'${escape(data['Confirmed'])}\', \'${date2}\'),`)
    }
  })
  .on('end', () => {
    client.query(query2.substring(0, query2.length - 1).concat(';'), async(err, res) => {
        console.log(err)
    });
    fs.unlinkSync('data2.csv')
  });;
}

const getData = async() => {
  var query1 = `insert into blabla values`
  var i = 0
  fs.createReadStream('test.csv')
  .pipe(csv())
  .on('data', (data) => {
      query1 = query1.concat(`(${Object.values(data).map((it) => { return `\'${escape(it)}\'`}).join(',') }),`)
  })
  .on('end', () => {
    client.query(query1.substring(0, query1.length - 1).concat(';'), async(err, res) => {
        console.log(err)
    });
  });;
}

app.get('/testCovidData', async (req, res) => {
    res.send("covidData")
})


app.get('/testPlaces', async (req, res) => {
  const places = await getPLacesLocation(req.query.latitude, req.query.longtitude, req.query.placeType)
  res.send(places)
})

app.listen(port, () => {
  console.log(`Example app listening at http://localhost:${port}`)
})

cron.schedule('00 00 11 * * 0-6', () => {
  getCovidData()
})