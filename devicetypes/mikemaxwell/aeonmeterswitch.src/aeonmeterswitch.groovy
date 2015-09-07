/**
 *  Copyright 2015 SmartThings
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
 	AES V1.0
    Mike Maxwell
 */
metadata {
	definition (name: "aeonMeterSwitch", namespace: "MikeMaxwell", author: "SmartThings,mmaxwell") {
		capability "Energy Meter"
		capability "Actuator"
		capability "Switch"
		capability "Power Meter"
		capability "Polling"
		capability "Refresh"
		capability "Configuration"
		capability "Sensor"

		command "reset"

		fingerprint inClusters: "0x25,0x32"
	}
preferences {
    	//state change reporting
       	input name: "param80", type: "enum", title: "State change notice:", description: "Type", required: true, options:["Off","Hail","Report"]
        
        //energy report, value change 0 - 32000 (watts)
        input name: "param91", type: "number", title: "Wattage change reporting(0=off):", description: "Watts (0-32000)", required: false
        
        //energy report, percent change 0 - 255 (percent)
        input name: "param92", type: "number", title: "Percent change reporting(0=off):", description: "Percent (1-100)", required: false
        
        //watt reporting interval 0 - 65535 (seconds)
        input name: "param111", type: "number", title: "Watt report interval(0=off):", description: "Seconds (0-65535)", required: false
        
        //KWH reporting interval 0 - 65535 (seconds)
        input name: "param112", type: "number", title: "KWH report interval(0=off):", description: "Seconds (0-65535)", required: false
        
        //input name: "blinker", type: "enum", title: "Set blinker mode:", description: "Blinker type", required: false, options:["Blink","Flasher","Strobe","5minute"]
}
	// simulator metadata
	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"

		for (int i = 0; i <= 10000; i += 1000) {
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		}

		// reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"

	}

	// tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
		valueTile("power", "device.power") {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy") {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat") {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch","power","energy"])
		details(["switch","power","energy","refresh","reset"])
	}
}

def updated() {
	def int param91 = settings.param91 ?: 0
    def int param92 = settings.param92 ?: 0
    def int param111 = settings.param111 ?: 0
    def int param112 = settings.param112 ?: 0
    def int param90 
    def int param101 //= 4
    def int param102 //= 8
    def int param103 = 0
    
    if (param91 + param92 == 0) param90 = 0
    else param90 = 1
    
    if (param111 == 0) param101 = 0
    else param101 = 4
    
	if (param112 == 0) param102 = 0
    else param102 = 8


    log.info "p90:${param90} p91:${param91} p92:${param92} p111:${param111} p112:${param112}"
    //log.info "msr:${state.MSR}"
	try {
		if (!state.MSR) {
			response(zwave.manufacturerSpecificV2.manufacturerSpecificGet().format())
		}
	} catch (e) { log.debug e }
    return response(delayBetween([
    		zwave.configurationV1.configurationSet(parameterNumber: 90, size: 1, scaledConfigurationValue: param90).format(),
			zwave.configurationV1.configurationSet(parameterNumber: 91, size: 2, scaledConfigurationValue: param91).format(),
			zwave.configurationV1.configurationSet(parameterNumber: 92, size: 1, scaledConfigurationValue: param92).format(),
            zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: param101).format(),
            zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: param102).format(),
            zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: param103).format(),
			zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: param111).format(),
			zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: param112).format()
            //,zwave.versionV1.versionGet().format()
            //,zwave.firmwareUpdateMdV1.firmwareMdGet().format()
	]))
}
	//version:VersionReport(applicationSubVersion: 43, applicationVersion: 1, zWaveLibraryType: 3, zWaveProtocolSubVersion: 78, zWaveProtocolVersion: 2)
        //class physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryGet 
        //                    zwave.switchBinaryV1.switchBinaryGet().format(),
        //class physicalgraph.zwave.commands.versionv1.VersionGet 
		//					  zwave.versionV1.versionGet().format()			
        //Firmware Md Get Command
		//class physicalgraph.zwave.commands.firmwareupdatemdv1.FirmwareMdGet 
        //                    zwave.firmwareUpdateMdV1.firmwareMdGet().format()
		//Firmware Md Report Command
		//class physicalgraph.zwave.commands.firmwareupdatemdv1.FirmwareMdReport 

def parse(String description) {
	def result = null
	if(description == "updated") return 
	def cmd = zwave.parse(description, [0x20: 1, 0x32: 1, 0x72: 2])
	if (cmd) {
		result = zwaveEvent(cmd)
	}
	return result
}
def zwaveEvent(physicalgraph.zwave.commands.firmwareupdatemdv1.FirmwareMdReport cmd) {
	log.info "firmware:${cmd}"
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	log.info "version:${cmd}"
    //version:VersionReport(applicationSubVersion: 43, applicationVersion: 1, zWaveLibraryType: 3, zWaveProtocolSubVersion: 78, zWaveProtocolVersion: 2)
}

//class physicalgraph.zwave.commands.versionv1.VersionReport 
//{
//Short	applicationSubVersion
//Short	applicationVersion
//Short	zWaveLibraryType
//Short	zWaveProtocolSubVersion
//Short	zWaveProtocolVersion
//List<Short>	payload
//String format()
//}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
	if (cmd.scale == 0) {
    	log.info ("kWh:${cmd.scaledMeterValue}")
		createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
	} else if (cmd.scale == 1) {
    	log.info ("kVAh:${cmd.scaledMeterValue}")
		createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
	} else if (cmd.scale == 2) {
    	log.info ("watts:${cmd.scaledMeterValue}")
		createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	def evt = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "physical")
	if (evt.isStateChange) {
		[evt, response(["delay 3000", zwave.meterV2.meterGet(scale: 2).format()])]
	} else {
		evt
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []
	//log.debug "cmd:${cmd}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	//log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	// retypeBasedOnMSR()

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)

	if (msr.startsWith("0086") && !state.aeonconfig) {  // Aeon Labs meter
		state.aeonconfig = 1
		result << response(delayBetween([
			zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 4).format(),   // report power in watts
			zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 300).format(), // every 5 min
			zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 8).format(),   // report energy in kWh
			zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 300).format(), // every 5 min
			zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: 0).format(),    // no third report
			//zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: 300).format(), // every 5 min
			zwave.meterV2.meterGet(scale: 0).format(),
			zwave.meterV2.meterGet(scale: 2).format(),
		]))
	} else {
		result << response(delayBetween([
			zwave.meterV2.meterGet(scale: 0).format(),
			zwave.meterV2.meterGet(scale: 2).format(),
		]))
	}

	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "$device.displayName: Unhandled: $cmd"
	[:]
}

def on() {
	[
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format(),
		"delay 3000",
		zwave.meterV2.meterGet(scale: 2).format()
	]
}

def off() {
	[
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format(),
		"delay 3000",
		zwave.meterV2.meterGet(scale: 2).format()
	]
}

def poll() {
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format()
	])
}

def refresh() {
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.meterV2.meterGet(scale: 0).format(),
		zwave.meterV2.meterGet(scale: 2).format()
	])
}

def configure() {
	zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
}

def reset() {
	return [
		zwave.meterV2.meterReset().format(),
		zwave.meterV2.meterGet(scale: 0).format()
	]
}