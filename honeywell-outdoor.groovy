/**
 *  Total Comfort API Outdoor Weather Sensor
 *
 *  Based on code by Eric Thomas
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
preferences {
    input("username", "text", title: "Username", description: "Your Total Comfort User Name")
    input("password", "password", title: "Password", description: "Your Total Comfort password")
    input("honeywelldevice", "text", title: "Device ID", description: "Your Device ID")

}
metadata {
	definition (name: "Total Comfort API Outdoor Weather Sensor", namespace: "bkeifer", author: "Brian Keifer") {
		capability "Polling"
		capability "Refresh"
		capability "Temperature Measurement"
		capability "Sensor"
        capability "Relative Humidity Measurement"
	}

	simulator {
		// TODO: define status and reply messages here
	}

   tiles {
        valueTile("temperature", "device.temperature", width: 2, height: 2, canChangeIcon: true) {
            state("temperature", label: '${currentValue}°F', unit:"F", backgroundColors: [
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
            )
        }
        valueTile("humidity", "device.humidity", inactiveLabel: false) {
            state "default", label:'${currentValue}%', unit:"Humidity"
        }

        //tile added for operating state - Create the tiles for each possible state, look at other examples if you wish to change the icons here.

        standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }

        main "temperature"
        details(["temperature", "humidity", "refresh",])
    }
}



// parse events into attributes
def parse(String description) {

}



def poll() {
refresh()
}

def setStatus() {

	data.SetStatus = 0

    login()
	log.debug "Executing 'setStatus'"
def today= new Date()
log.debug "https://rs.alarmnet.com/TotalConnectComfort/Device/SubmitControlScreenChanges"


    def params = [
        uri: "https://rs.alarmnet.com/TotalConnectComfort/Device/SubmitControlScreenChanges",
        headers: [
              'Accept': 'application/json, text/javascript, */*; q=0.01',
              'DNT': '1',
			  'Accept-Encoding': 'gzip,deflate,sdch',
              'Cache-Control': 'max-age=0',
              'Accept-Language': 'en-US,en,q=0.8',
              'Connection': 'keep-alive',
              'Host': 'rs.alarmnet.com',
              'Referer': "https://rs.alarmnet.com/TotalConnectComfort/Device/Control/${settings.honeywelldevice}",
              'X-Requested-With': 'XMLHttpRequest',
              'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36',
              'Cookie': data.cookiess        ],
        body: [ DeviceID: "${settings.honeywelldevice}", SystemSwitch : data.SystemSwitch ,HeatSetpoint : data.HeatSetpoint, CoolSetpoint: data.CoolSetpoint, HeatNextPeriod: data.HeatNextPeriod,CoolNextPeriod:data.CoolNextPeriod,StatusHeat:data.StatusHeat,StatusCool:data.StatusCool,FanMode:data.FanMode]

]

    httpPost(params) { response ->
        log.debug "Request was successful, $response.status"

    }

    log.debug "SetStatus is 1 now"
    data.SetStatus = 1

}

def getStatus() {
	log.debug "Executing 'getStatus'"
def today= new Date()
log.debug "https://rs.alarmnet.com/TotalConnectComfort/Device/CheckDataSession/${settings.honeywelldevice}?_=$today.time"



    def params = [
        uri: "https://rs.alarmnet.com/TotalConnectComfort/Device/CheckDataSession/${settings.honeywelldevice}",
        headers: [
              'Accept': '*/*',
              'DNT': '1',
              'Accept-Encoding': 'plain',
              'Cache-Control': 'max-age=0',
              'Accept-Language': 'en-US,en,q=0.8',
              'Connection': 'keep-alive',
              'Host': 'rs.alarmnet.com',
              'Referer': 'https://rs.alarmnet.com/TotalConnectComfort/',
              'X-Requested-With': 'XMLHttpRequest',
              'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36',
              'Cookie': data.cookiess        ],
    ]

    httpGet(params) { response ->
        log.debug "Request was successful, $response.status"

		def OutdoorTemp = response.data.latestData.uiData.OutdoorTemperature
        def OutdoorHumidity = response.data.latestData.uiData.OutdoorHumidity

        sendEvent(name: 'temperature', value: OutdoorTemp as Integer)
        sendEvent(name: 'humidity', value: OutdoorHumidity as Integer)
    }

}

def api(method, args = [], success = {}) {

}

// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success) {

}

def refresh() {
	log.debug "Executing 'refresh'"
    login()
    getStatus()
}

def login() {
	log.debug "Executing 'login'"



    def params = [
        uri: 'https://rs.alarmnet.com/TotalConnectComfort/',
        headers: [
            'Content-Type': 'application/x-www-form-urlencoded',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Encoding': 'sdch',
            'Host': 'rs.alarmnet.com',
            'DNT': '1',
            'Origin': 'https://rs.alarmnet.com/TotalComfort/',
            'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36'
        ],
        body: [timeOffset: '240', UserName: "${settings.username}", Password: "${settings.password}", RememberMe: 'false']
    ]

	data.cookiess = ''

    httpPost(params) { response ->
        log.debug "Request was successful, $response.status"
        log.debug response.headers
		response.getHeaders('Set-Cookie').each {
        	String cookie = it.value.split(';|,')[0]
			log.debug "Adding cookie to collection: $cookie"
            if(cookie != ".ASPXAUTH_TH_A=") {
			data.cookiess = data.cookiess+cookie+';'
            }
        }
        //log.debug "cookies: $data.cookies"

    }


}

def isLoggedIn() {
    if(!data.auth) {
        log.debug "No data.auth"
        return false
    }

    def now = new Date().getTime();
    return data.auth.expires_in > now
}
