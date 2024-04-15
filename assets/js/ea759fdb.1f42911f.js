"use strict";(self.webpackChunkdagger=self.webpackChunkdagger||[]).push([[5278],{5680:(e,t,n)=>{n.d(t,{xA:()=>p,yg:()=>d});var a=n(6540);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function o(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,a,r=function(e,t){if(null==e)return{};var n,a,r={},i=Object.keys(e);for(a=0;a<i.length;a++)n=i[a],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(a=0;a<i.length;a++)n=i[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var l=a.createContext({}),c=function(e){var t=a.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):o(o({},t),e)),n},p=function(e){var t=c(e.components);return a.createElement(l.Provider,{value:t},e.children)},g="mdxType",u={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},m=a.forwardRef((function(e,t){var n=e.components,r=e.mdxType,i=e.originalType,l=e.parentName,p=s(e,["components","mdxType","originalType","parentName"]),g=c(n),m=r,d=g["".concat(l,".").concat(m)]||g[m]||u[m]||i;return n?a.createElement(d,o(o({ref:t},p),{},{components:n})):a.createElement(d,o({ref:t},p))}));function d(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var i=n.length,o=new Array(i);o[0]=m;var s={};for(var l in t)hasOwnProperty.call(t,l)&&(s[l]=t[l]);s.originalType=e,s[g]="string"==typeof e?e:r,o[1]=s;for(var c=2;c<i;c++)o[c]=n[c];return a.createElement.apply(null,o)}return a.createElement.apply(null,n)}m.displayName="MDXCreateElement"},271:(e,t,n)=>{n.r(t),n.d(t,{contentTitle:()=>o,default:()=>g,frontMatter:()=>i,metadata:()=>s,toc:()=>l});var a=n(8168),r=(n(6540),n(5680));const i={},o="API Monitoring",s={unversionedId:"usecase/api_monitoring",id:"usecase/api_monitoring",isDocsHomePage:!1,title:"API Monitoring",description:"API Monitoring refers to the practice of monitoring APIs in production to gain visibility into performance, availability and functional correctness. You want to get some insights about the metrics in real-time as soon as possible and possibly have some alerting mechanisms for unusual behaviour.",source:"@site/docs/usecase/api_monitoring.md",sourceDirName:"usecase",slug:"/usecase/api_monitoring",permalink:"/dagger/docs/usecase/api_monitoring",editUrl:"https://github.com/goto/dagger/edit/master/docs/docs/usecase/api_monitoring.md",tags:[],version:"current",frontMatter:{},sidebar:"docsSidebar",previous:{title:"Overview",permalink:"/dagger/docs/usecase/overview"},next:{title:"Feature Ingestion",permalink:"/dagger/docs/usecase/feature_ingestion"}},l=[{value:"How Dagger Solves?",id:"how-dagger-solves",children:[{value:"Sample Schema Definition",id:"sample-schema-definition",children:[]},{value:"Sample Query in Dagger",id:"sample-query-in-dagger",children:[]},{value:"Impact",id:"impact",children:[]}]}],c={toc:l},p="wrapper";function g(e){let{components:t,...i}=e;return(0,r.yg)(p,(0,a.A)({},c,i,{components:t,mdxType:"MDXLayout"}),(0,r.yg)("h1",{id:"api-monitoring"},"API Monitoring"),(0,r.yg)("p",null,"API Monitoring refers to the practice of monitoring APIs in production to gain visibility into performance, availability and functional correctness. You want to get some insights about the metrics in real-time as soon as possible and possibly have some alerting mechanisms for unusual behaviour."),(0,r.yg)("h2",{id:"how-dagger-solves"},"How Dagger Solves?"),(0,r.yg)("p",null,"If you look closely, API health/uptime/performance is mostly a ",(0,r.yg)("em",{parentName:"p"},"real-time streaming aggregation problem")," and hence Dagger can be a tool of choice. The only prerequisite is that you need some sort of structured events to be streamed for each API calls. The events can be streamed by the middleware/API gateway layer with all sort of information."),(0,r.yg)("p",null,"This is sample Data flow of API monitoring in Dagger.\n",(0,r.yg)("img",{alt:"API Monitoring",src:n(3953).A})),(0,r.yg)("h3",{id:"sample-schema-definition"},"Sample Schema Definition"),(0,r.yg)("p",null,"This is a sample schema of API logs."),(0,r.yg)("pre",null,(0,r.yg)("code",{parentName:"pre",className:"language-protobuf"},"message SampleAPILog {\n    google.protobuf.Timestamp event_timestamp = 1;\n    int32 http_status_code = 2;\n    string api_method = 13;\n    string api_id = 14;\n    string api_uri = 15;\n    string api_name = 16;\n    string api_upstream_url = 17;\n}\n")),(0,r.yg)("h3",{id:"sample-query-in-dagger"},"Sample Query in Dagger"),(0,r.yg)("p",null,"We want to aggregate APIs on status codes. This will help us bucket APIs which are responding 4XX or 5XX and possibly have some issues."),(0,r.yg)("pre",null,(0,r.yg)("code",{parentName:"pre",className:"language-SQL"},"SELECT\n  api_name AS api_name,\n  api_uri AS api_uri,\n  api_method AS api_method,\n  Cast(http_status_code AS BIGINT) as http_status_code,\n  count(1) as request_count,\n  Tumble_end(rowtime, INTERVAL '60' second) AS event_timestamp\nFROM\n  `api_logs`\nGROUP BY\n  api_name,\n  api_uri,\n  http_status_code,\n  api_method,\n  Tumble (rowtime, INTERVAL '60' second)\n")),(0,r.yg)("p",null,"This is a Dashboard on a sample API based on the status code powered by Dagger."),(0,r.yg)("p",null,(0,r.yg)("img",{alt:"Profile Enrichment",src:n(1465).A})),(0,r.yg)("h3",{id:"impact"},"Impact"),(0,r.yg)("ul",null,(0,r.yg)("li",{parentName:"ul"},"Since Dagger support influx as one of the sinks, user can easily set up alerts and visualise API performance on some visualization tools in near real-time."),(0,r.yg)("li",{parentName:"ul"},"Apart from API uptime all sort of other performance details per API like endpoint latency, proxy latency can be aggregated by Dagger."),(0,r.yg)("li",{parentName:"ul"},"Apart from simple health monitoring you can do a lot of complex streaming analysis on the middleware logs. Since Dagger scales in an instant, the scale of API event logs should not be an issue.")))}g.isMDXComponent=!0},3953:(e,t,n)=>{n.d(t,{A:()=>a});const a=n.p+"assets/images/api-monitoring-e1b2c92d7cf87a97dedf15f54c16c9e7.png"},1465:(e,t,n)=>{n.d(t,{A:()=>a});const a=n.p+"assets/images/api-status-08546be3cc26f033417f05a4c5a75f9a.png"}}]);