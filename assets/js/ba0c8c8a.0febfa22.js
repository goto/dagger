"use strict";(self.webpackChunkdagger=self.webpackChunkdagger||[]).push([[8398],{3905:function(e,t,r){r.d(t,{Zo:function(){return d},kt:function(){return m}});var a=r(7294);function n(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function i(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,a)}return r}function o(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?i(Object(r),!0).forEach((function(t){n(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):i(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function s(e,t){if(null==e)return{};var r,a,n=function(e,t){if(null==e)return{};var r,a,n={},i=Object.keys(e);for(a=0;a<i.length;a++)r=i[a],t.indexOf(r)>=0||(n[r]=e[r]);return n}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(a=0;a<i.length;a++)r=i[a],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(n[r]=e[r])}return n}var c=a.createContext({}),l=function(e){var t=a.useContext(c),r=t;return e&&(r="function"==typeof e?e(t):o(o({},t),e)),r},d=function(e){var t=l(e.components);return a.createElement(c.Provider,{value:t},e.children)},u={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},p=a.forwardRef((function(e,t){var r=e.components,n=e.mdxType,i=e.originalType,c=e.parentName,d=s(e,["components","mdxType","originalType","parentName"]),p=l(r),m=n,f=p["".concat(c,".").concat(m)]||p[m]||u[m]||i;return r?a.createElement(f,o(o({ref:t},d),{},{components:r})):a.createElement(f,o({ref:t},d))}));function m(e,t){var r=arguments,n=t&&t.mdxType;if("string"==typeof e||n){var i=r.length,o=new Array(i);o[0]=p;var s={};for(var c in t)hasOwnProperty.call(t,c)&&(s[c]=t[c]);s.originalType=e,s.mdxType="string"==typeof e?e:n,o[1]=s;for(var l=2;l<i;l++)o[l]=r[l];return a.createElement.apply(null,o)}return a.createElement.apply(null,r)}p.displayName="MDXCreateElement"},8528:function(e,t,r){r.r(t),r.d(t,{frontMatter:function(){return s},contentTitle:function(){return c},metadata:function(){return l},toc:function(){return d},default:function(){return p}});var a=r(7462),n=r(3366),i=(r(7294),r(3905)),o=["components"],s={},c="Darts",l={unversionedId:"advance/DARTS",id:"advance/DARTS",isDocsHomePage:!1,title:"Darts",description:"In data streaming pipelines, in certain cases, not entire data is present in the event itself. One scenario for such cases can be where some particular information is present in form of static data that you need in runtime. DARTS(Dagger Refer-Table Service) allows you to join streaming data from a reference data store. It supports reference data store in the form of a list or  map. It enables the refer-table with the help of UDFs which can be used in the SQL query. Currently, we only support GCS as a reference data source.",source:"@site/docs/advance/DARTS.md",sourceDirName:"advance",slug:"/advance/DARTS",permalink:"/dagger/docs/advance/DARTS",editUrl:"https://github.com/odpf/dagger/edit/master/docs/docs/advance/DARTS.md",tags:[],version:"current",frontMatter:{},sidebar:"docsSidebar",previous:{title:"Longbow+",permalink:"/dagger/docs/advance/longbow_plus"},next:{title:"Overview",permalink:"/dagger/docs/usecase/overview"}},d=[{value:"DartGet",id:"dartget",children:[{value:"Example",id:"example",children:[]}]},{value:"DartContains",id:"dartcontains",children:[{value:"Example",id:"example-1",children:[]}]},{value:"Configurations",id:"configurations",children:[]},{value:"Properties common to both DARTS UDFs",id:"properties-common-to-both-darts-udfs",children:[{value:"Persistence layer",id:"persistence-layer",children:[]},{value:"Caching mechanism",id:"caching-mechanism",children:[]},{value:"Caching refresh rate",id:"caching-refresh-rate",children:[]},{value:"Updates in data",id:"updates-in-data",children:[]}]}],u={toc:d};function p(e){var t=e.components,r=(0,n.Z)(e,o);return(0,i.kt)("wrapper",(0,a.Z)({},u,r,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("h1",{id:"darts"},"Darts"),(0,i.kt)("p",null,"In data streaming pipelines, in certain cases, not entire data is present in the event itself. One scenario for such cases can be where some particular information is present in form of static data that you need in runtime. DARTS(Dagger Refer-Table Service) allows you to join streaming data from a reference data store. It supports reference data store in the form of a list or <key, value> map. It enables the refer-table with the help of ",(0,i.kt)("a",{parentName:"p",href:"/dagger/docs/guides/use_udf"},"UDFs")," which can be used in the SQL query. Currently, we only support GCS as a reference data source."),(0,i.kt)("h1",{id:"types-of-darts"},"Types of DARTS"),(0,i.kt)("p",null,"We currently support only ",(0,i.kt)("a",{parentName:"p",href:"https://cloud.google.com/storage"},"GCS")," as an external static data source. In order to utilize this data directly into Dagger SQL queries, we have enabled two different types of functions."),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("a",{parentName:"li",href:"/dagger/docs/advance/DARTS#dartget"},"DartGet")),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("a",{parentName:"li",href:"/dagger/docs/advance/DARTS#dartcontains"},"DartContains"))),(0,i.kt)("h2",{id:"dartget"},"DartGet"),(0,i.kt)("p",null,"This UDF can be used in cases where we want to fetch static information from a <key, value> mapping. In this case, the key can be a field from input Kafka topic and corresponding value can be fetched from remote store(GCS). You can read more about this UDF ",(0,i.kt)("a",{parentName:"p",href:"/dagger/docs/reference/udfs#dartget"},"here"),"."),(0,i.kt)("h3",{id:"example"},"Example"),(0,i.kt)("p",null,"Let\u2019s assume we need to find out the number of bookings getting completed in a particular District per minute. The input schema only has information regarding service_area_id but not the District. The mapping of service_area_id to District is present in a static key-value map. We can utilize DartGet in order to get this information in our query."),(0,i.kt)("p",{align:"center"},(0,i.kt)("img",{src:"/img/dart-get.png",width:"80%"})),(0,i.kt)("p",null,"Sample input schema for booking"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-protobuf"},"message SampleBookingInfo {\n  string order_number = 1;\n  string order_url = 2;\n  Status.Enum status = 3;\n  google.protobuf.Timestamp event_timestamp = 4;\n  string customer_id = 5;\n  string service_area_id = 6;\n}\n")),(0,i.kt)("p",null,"Sample static data for serviceAreaId-to-district mapping"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-JSON"},'{\n  "1": "district1",\n  "2": "district2",\n  "3": "district3",\n  "4": "district4"\n}\n')),(0,i.kt)("p",null,"Sample Query"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-SQL"},"# here booking denotes the booking events stream with the sample input schema\nSELECT\n  COUNT(DISTINCT order_number) as completed_bookings,\n  DartGet('serviceAreaId-to-district/data.json', service_area_id, 24) AS district,\n  TUMBLE_END(rowtime, INTERVAL '60' SECOND) AS window_timestamp\nFROM\n  booking\nWHERE\n  status = 'COMPLETED'\nGROUP BY\n  DartGet('serviceAreaId-to-district/data.json', service_area_id, 24),\n  TUMBLE (rowtime, INTERVAL '60' SECOND)\n")),(0,i.kt)("h2",{id:"dartcontains"},"DartContains"),(0,i.kt)("p",null,"This UDF can be used in cases where we want to verify the presence of a key in a static list present remotely in GCS. You can read more about this UDF ",(0,i.kt)("a",{parentName:"p",href:"/dagger/docs/reference/udfs#dartcontains"},"here"),"."),(0,i.kt)("h3",{id:"example-1"},"Example"),(0,i.kt)("p",null,"Let\u2019s assume we need to count the number of completed bookings per minute but excluding a few blacklisted customers. This static list of blacklisted customers is present remotely. We can utilize DartContains UDF here."),(0,i.kt)("p",{align:"center"},(0,i.kt)("img",{src:"/img/dart-contains.png",width:"80%"})),(0,i.kt)("p",null,"Sample input schema for booking"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-protobuf"},"message SampleBookingInfo {\n  string order_number = 1;\n  string order_url = 2;\n  Status.Enum status = 3;\n  google.protobuf.Timestamp event_timestamp = 4;\n  string customer_id = 5;\n  string service_area_id = 6;\n}\n")),(0,i.kt)("p",null,"Sample static data for blacklisted-customers"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-JSON"},'{\n  "data": ["1", "2", "3", "4"]\n}\n')),(0,i.kt)("p",null,"Sample Query"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-SQL"},"# here booking denotes the booking events stream with the sample input schema\nSELECT\n  COUNT(DISTINCT order_number) as completed_bookings,\n  TUMBLE_END(rowtime, INTERVAL '60' SECOND) AS window_timestamp\nFROM\n  booking\nWHERE\n  status = 'COMPLETED' AND\n  NOT DartContains('blacklisted-customers/data.json', customer_id, 24)\nGROUP BY\n  TUMBLE (rowtime, INTERVAL '60' SECOND)\n")),(0,i.kt)("p",null,(0,i.kt)("strong",{parentName:"p"},"Note:")," To use DartContains you need to provide the data in the form of a JSON array with Key as ",(0,i.kt)("inlineCode",{parentName:"p"},"data")," always."),(0,i.kt)("h2",{id:"configurations"},"Configurations"),(0,i.kt)("p",null,"Most of DARTS configurations are via UDF contract, for other glabal configs refer ",(0,i.kt)("a",{parentName:"p",href:"/dagger/docs/reference/configuration#darts"},"here"),"."),(0,i.kt)("h2",{id:"properties-common-to-both-darts-udfs"},"Properties common to both DARTS UDFs"),(0,i.kt)("h3",{id:"persistence-layer"},"Persistence layer"),(0,i.kt)("p",null,"We store all data references in GCS. We recommend using GCS where the data reference is not too big and updates are very few. GCS also offers a cost-effective solution.\nWe currently support storing data in a specific bucket, with a custom folder for every dart. The data is always kept in a file and you have to pass the relative path ",(0,i.kt)("inlineCode",{parentName:"p"},"<custom-folder>/filename.json"),". There should not be many reads as every time we read the list from GCS we read the whole selected list and cache it in Dagger."),(0,i.kt)("h3",{id:"caching-mechanism"},"Caching mechanism"),(0,i.kt)("p",null,"Dart fetches the data from GCS after configurable refresh period or when entire data is missing from the cache or is empty. After Dart fetches the data, it stores it in the application state."),(0,i.kt)("h3",{id:"caching-refresh-rate"},"Caching refresh rate"),(0,i.kt)("p",null,"We have defined the refresh rate in hours. Users can set the refresh rate from the UDF contract. We set the default value as one hour in case Dart users don't bother about the refresh rate."),(0,i.kt)("h3",{id:"updates-in-data"},"Updates in data"),(0,i.kt)("p",null,"There is a refresh rate as part of the UDF. This defines when the data will be reloaded into memory from the data store. So after one cycle from manual data update, the changes will reflect into the Dagger."))}p.isMDXComponent=!0}}]);