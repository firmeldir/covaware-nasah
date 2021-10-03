const {Client} = require("@googlemaps/google-maps-services-js");

const client = new Client({});

 const getCityAndCounty = async (lat, lng) => {

  const cityAndCountry = await client.geocode({
    params : {
      latlng: `${lat}, ${lng}`,
      key: process.env.GOOGLE_API_KEY
    },
    timeout: 1000,
  })
    
  const data = cityAndCountry.data.results[0].address_components
  const county = data[3].long_name;
  const state = data[5].long_name;
  return {
    county,
    state
  }
}

module.exports = {getCityAndCounty};