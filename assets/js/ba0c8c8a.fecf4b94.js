"use strict";(self.webpackChunkdagger=self.webpackChunkdagger||[]).push([[8398],{3905:(e,t,a)=>{a.d(t,{Zo:()=>d,kt:()=>f});var r=a(7294);function n(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}function i(e,t){var a=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),a.push.apply(a,r)}return a}function o(e){for(var t=1;t<arguments.length;t++){var a=null!=arguments[t]?arguments[t]:{};t%2?i(Object(a),!0).forEach((function(t){n(e,t,a[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(a)):i(Object(a)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(a,t))}))}return e}function s(e,t){if(null==e)return{};var a,r,n=function(e,t){if(null==e)return{};var a,r,n={},i=Object.keys(e);for(r=0;r<i.length;r++)a=i[r],t.indexOf(a)>=0||(n[a]=e[a]);return n}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(r=0;r<i.length;r++)a=i[r],t.indexOf(a)>=0||Object.prototype.propertyIsEnumerable.call(e,a)&&(n[a]=e[a])}return n}var c=r.createContext({}),l=function(e){var t=r.useContext(c),a=t;return e&&(a="function"==typeof e?e(t):o(o({},t),e)),a},d=function(e){var t=l(e.components);return r.createElement(c.Provider,{value:t},e.children)},p="mdxType",u={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},m=r.forwardRef((function(e,t){var a=e.components,n=e.mdxType,i=e.originalType,c=e.parentName,d=s(e,["components","mdxType","originalType","parentName"]),p=l(a),m=n,f=p["".concat(c,".").concat(m)]||p[m]||u[m]||i;return a?r.createElement(f,o(o({ref:t},d),{},{components:a})):r.createElement(f,o({ref:t},d))}));function f(e,t){var a=arguments,n=t&&t.mdxType;if("string"==typeof e||n){var i=a.length,o=new Array(i);o[0]=m;var s={};for(var c in t)hasOwnProperty.call(t,c)&&(s[c]=t[c]);s.originalType=e,s[p]="string"==typeof e?e:n,o[1]=s;for(var l=2;l<i;l++)o[l]=a[l];return r.createElement.apply(null,o)}return r.createElement.apply(null,a)}m.displayName="MDXCreateElement"},8528:(e,t,a)=>{a.r(t),a.d(t,{contentTitle:()=>o,default:()=>p,frontMatter:()=>i,metadata:()=>s,toc:()=>c});var r=a(7462),n=(a(7294),a(3905));const i={},o="Darts",s={unversionedId:"advance/DARTS",id:"advance/DARTS",isDocsHomePage:!1,title:"Darts",description:"In data streaming pipelines, in certain cases, not entire data is present in the event itself. One scenario for such cases can be where some particular information is present in form of static data that you need in runtime. DARTS(Dagger Refer-Table Service) allows you to join streaming data from a reference data store. It supports reference data store in the form of a list or  map. It enables the refer-table with the help of UDFs which can be used in the SQL query. Currently, we only support GCS as a reference data source.",source:"@site/docs/advance/DARTS.md",sourceDirName:"advance",slug:"/advance/DARTS",permalink:"/dagger/docs/advance/DARTS",editUrl:"https://github.com/goto/dagger/edit/master/docs/docs/advance/DARTS.md",tags:[],version:"current",frontMatter:{},sidebar:"docsSidebar",previous:{title:"Longbow+",permalink:"/dagger/docs/advance/longbow_plus"},next:{title:"Security",permalink:"/dagger/docs/advance/security"}},c=[{value:"DartGet",id:"dartget",children:[{value:"Example",id:"example",children:[]}]},{value:"DartContains",id:"dartcontains",children:[{value:"Example",id:"example-1",children:[]}]},{value:"Configurations",id:"configurations",children:[]},{value:"Properties common to both DARTS UDFs",id:"properties-common-to-both-darts-udfs",children:[{value:"Persistence layer",id:"persistence-layer",children:[]},{value:"Caching mechanism",id:"caching-mechanism",children:[]},{value:"Caching refresh rate",id:"caching-refresh-rate",children:[]},{value:"Updates in data",id:"updates-in-data",children:[]}]}],l={toc:c},d="wrapper";function p(e){let{components:t,...i}=e;return(0,n.kt)(d,(0,r.Z)({},l,i,{components:t,mdxType:"MDXLayout"}),(0,n.kt)("h1",{id:"darts"},"Darts"),(0,n.kt)("p",null,"In data streaming pipelines, in certain cases, not entire data is present in the event itself. One scenario for such cases can be where some particular information is present in form of static data that you need in runtime. DARTS(Dagger Refer-Table Service) allows you to join streaming data from a reference data store. It supports reference data store in the form of a list or <key, value> map. It enables the refer-table with the help of ",(0,n.kt)("a",{parentName:"p",href:"/dagger/docs/guides/use_udf"},"UDFs")," which can be used in the SQL query. Currently, we only support GCS as a reference data source."),(0,n.kt)("h1",{id:"types-of-darts"},"Types of DARTS"),(0,n.kt)("p",null,"We currently support only ",(0,n.kt)("a",{parentName:"p",href:"https://cloud.google.com/storage"},"GCS")," as an external static data source. In order to utilize this data directly into Dagger SQL queries, we have enabled two different types of functions."),(0,n.kt)("ul",null,(0,n.kt)("li",{parentName:"ul"},(0,n.kt)("a",{parentName:"li",href:"/dagger/docs/advance/DARTS#dartget"},"DartGet")),(0,n.kt)("li",{parentName:"ul"},(0,n.kt)("a",{parentName:"li",href:"/dagger/docs/advance/DARTS#dartcontains"},"DartContains"))),(0,n.kt)("h2",{id:"dartget"},"DartGet"),(0,n.kt)("p",null,"This UDF can be used in cases where we want to fetch static information from a <key, value> mapping. In this case, the key can be a field from input Kafka topic and corresponding value can be fetched from remote store(GCS). You can read more about this UDF ",(0,n.kt)("a",{parentName:"p",href:"/dagger/docs/reference/udfs#dartget"},"here"),"."),(0,n.kt)("h3",{id:"example"},"Example"),(0,n.kt)("p",null,"Let\u2019s assume we need to find out the number of bookings getting completed in a particular District per minute. The input schema only has information regarding service_area_id but not the District. The mapping of service_area_id to District is present in a static key-value map. We can utilize DartGet in order to get this information in our query."),(0,n.kt)("p",null,(0,n.kt)("img",{src:a(855).Z})),(0,n.kt)("p",null,"Sample input schema for booking"),(0,n.kt)("pre",null,(0,n.kt)("code",{parentName:"pre",className:"language-protobuf"},"message SampleBookingInfo {\n  string order_number = 1;\n  string order_url = 2;\n  Status.Enum status = 3;\n  google.protobuf.Timestamp event_timestamp = 4;\n  string customer_id = 5;\n  string service_area_id = 6;\n}\n")),(0,n.kt)("p",null,"Sample static data for serviceAreaId-to-district mapping"),(0,n.kt)("pre",null,(0,n.kt)("code",{parentName:"pre",className:"language-JSON"},'{\n  "1": "district1",\n  "2": "district2",\n  "3": "district3",\n  "4": "district4"\n}\n')),(0,n.kt)("p",null,"Sample Query"),(0,n.kt)("pre",null,(0,n.kt)("code",{parentName:"pre",className:"language-SQL"},"# here booking denotes the booking events stream with the sample input schema\nSELECT\n  COUNT(DISTINCT order_number) as completed_bookings,\n  DartGet('serviceAreaId-to-district/data.json', service_area_id, 24) AS district,\n  TUMBLE_END(rowtime, INTERVAL '60' SECOND) AS window_timestamp\nFROM\n  booking\nWHERE\n  status = 'COMPLETED'\nGROUP BY\n  DartGet('serviceAreaId-to-district/data.json', service_area_id, 24),\n  TUMBLE (rowtime, INTERVAL '60' SECOND)\n")),(0,n.kt)("h2",{id:"dartcontains"},"DartContains"),(0,n.kt)("p",null,"This UDF can be used in cases where we want to verify the presence of a key in a static list present remotely in GCS. You can read more about this UDF ",(0,n.kt)("a",{parentName:"p",href:"/dagger/docs/reference/udfs#dartcontains"},"here"),"."),(0,n.kt)("h3",{id:"example-1"},"Example"),(0,n.kt)("p",null,"Let\u2019s assume we need to count the number of completed bookings per minute but excluding a few blacklisted customers. This static list of blacklisted customers is present remotely. We can utilize DartContains UDF here."),(0,n.kt)("p",null,(0,n.kt)("img",{src:a(4529).Z})),(0,n.kt)("p",null,"Sample input schema for booking"),(0,n.kt)("pre",null,(0,n.kt)("code",{parentName:"pre",className:"language-protobuf"},"message SampleBookingInfo {\n  string order_number = 1;\n  string order_url = 2;\n  Status.Enum status = 3;\n  google.protobuf.Timestamp event_timestamp = 4;\n  string customer_id = 5;\n  string service_area_id = 6;\n}\n")),(0,n.kt)("p",null,"Sample static data for blacklisted-customers"),(0,n.kt)("pre",null,(0,n.kt)("code",{parentName:"pre",className:"language-JSON"},'{\n  "data": ["1", "2", "3", "4"]\n}\n')),(0,n.kt)("p",null,"Sample Query"),(0,n.kt)("pre",null,(0,n.kt)("code",{parentName:"pre",className:"language-SQL"},"# here booking denotes the booking events stream with the sample input schema\nSELECT\n  COUNT(DISTINCT order_number) as completed_bookings,\n  TUMBLE_END(rowtime, INTERVAL '60' SECOND) AS window_timestamp\nFROM\n  booking\nWHERE\n  status = 'COMPLETED' AND\n  NOT DartContains('blacklisted-customers/data.json', customer_id, 24)\nGROUP BY\n  TUMBLE (rowtime, INTERVAL '60' SECOND)\n")),(0,n.kt)("p",null,(0,n.kt)("strong",{parentName:"p"},"Note:")," To use DartContains you need to provide the data in the form of a JSON array with Key as ",(0,n.kt)("inlineCode",{parentName:"p"},"data")," always."),(0,n.kt)("h2",{id:"configurations"},"Configurations"),(0,n.kt)("p",null,"Most of DARTS configurations are via UDF contract, for other glabal configs refer ",(0,n.kt)("a",{parentName:"p",href:"/dagger/docs/reference/configuration#darts"},"here"),"."),(0,n.kt)("h2",{id:"properties-common-to-both-darts-udfs"},"Properties common to both DARTS UDFs"),(0,n.kt)("h3",{id:"persistence-layer"},"Persistence layer"),(0,n.kt)("p",null,"We store all data references in GCS. We recommend using GCS where the data reference is not too big and updates are very few. GCS also offers a cost-effective solution.\nWe currently support storing data in a specific bucket, with a custom folder for every dart. The data is always kept in a file and you have to pass the relative path ",(0,n.kt)("inlineCode",{parentName:"p"},"<custom-folder>/filename.json"),". There should not be many reads as every time we read the list from GCS we read the whole selected list and cache it in Dagger."),(0,n.kt)("h3",{id:"caching-mechanism"},"Caching mechanism"),(0,n.kt)("p",null,"Dart fetches the data from GCS after configurable refresh period or when entire data is missing from the cache or is empty. After Dart fetches the data, it stores it in the application state."),(0,n.kt)("h3",{id:"caching-refresh-rate"},"Caching refresh rate"),(0,n.kt)("p",null,"We have defined the refresh rate in hours. Users can set the refresh rate from the UDF contract. We set the default value as one hour in case Dart users don't bother about the refresh rate."),(0,n.kt)("h3",{id:"updates-in-data"},"Updates in data"),(0,n.kt)("p",null,"There is a refresh rate as part of the UDF. This defines when the data will be reloaded into memory from the data store. So after one cycle from manual data update, the changes will reflect into the Dagger."))}p.isMDXComponent=!0},4529:(e,t,a)=>{a.d(t,{Z:()=>r});const r=a.p+"assets/images/dart-contains-9f964f3ea640ae9a77e21f8f8600f612.png"},855:(e,t,a)=>{a.d(t,{Z:()=>r});const r=a.p+"assets/images/dart-get-9b68542a0d8dc22969df00ab12d399ef.png"}}]);