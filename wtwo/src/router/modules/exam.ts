const examManage = () => import("@/pages/examManage/examManage.vue");
export default [
    {
        path: "/examManage",
        name: "examManage",
        component: examManage,
    },
    {
        path: "/stuScoreManage",
        name: "stuScoreManage",
        component: () => import("@/pages/examManage/stuScoreManage/stuScoreManage.vue"),
    }
]