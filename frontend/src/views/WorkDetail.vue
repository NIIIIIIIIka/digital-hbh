<template>
  <div class="artwork-detail-page">
    
  </div>
</template>

<script>
// 引入作品数据库

export default {
  name: 'ArtworkDetail',
  
  data() {
    return {
      artworksDatabase : [
            // 拟孙雪居意（原来的默认作品）
             {
                id: 1,
                title: "拟孙雪居意",
                year: "1949",
                artist: "黄宾虹",
                size: "纵82厘米，横42厘米",
                material: "纸本设色",
                collection: "浙江省博物馆",
                period: "晚年成熟期",
                description: "创作于黄宾虹晚年艺术风格从清逸疏朗向浑厚华滋转型的关键阶段。作品借鉴了明代画家孙克弘（号雪居）的笔意风格，以自然山水为题材，通过朦胧苍翠的树木、深秀葳蕤的山石与错落有致的屋宇，营造出别具一格的生机盎然意境。画面采用渍墨与渍色结合的创新技法，以斑驳墨线勾勒轮廓，湿笔晕染辅以流彩着色，实现了色墨交融的视觉效果。这种技法在传统水墨基础上有所突破，体现了黄宾虹'五笔七墨'理论的实践。",
                tags: ["晚年作品", "山水画", "纸本设色", "积墨技法", "浙江省博物馆藏"],
                techniques: ["积墨法", "破墨法", "渍墨法", "渍色法"],
                features: [
                    "积墨、破墨技法的娴熟运用",
                    "色墨交融，浑厚华滋",
                    "构图严谨，虚实相生",
                    "笔法苍劲，墨色淋漓",
                    "意境深远，气韵生动"
                ],
                analysis: [
                    {
                        title: "笔墨技法",
                        icon: "fas fa-paint-brush",
                        content: "采用'五笔七墨'理论，积墨数十遍而不滞，破墨求其润泽，焦墨显其精神。"
                    },
                    {
                        title: "色彩运用",
                        icon: "fas fa-palette",
                        content: "以墨为主，色为辅，色墨交融，形成独特的'黄宾虹黑'，深沉而透亮。"
                    },
                    {
                        title: "构图特点",
                        icon: "fas fa-mountain",
                        content: "采用'三远法'结合，高远、深远、平远兼备，画面层次丰富。"
                    }
                ],
                images: [
                    { type: "main", src: "./images/拟孙雪居意.jpg", alt: "黄宾虹《拟孙雪居意》整体图" },
                    { type: "detail", src: "./images/detail1.jpg", alt: "树木细节" },
                    { type: "detail", src: "./images/detail2.jpg", alt: "山石细节" },
                    { type: "detail", src: "./images/detail3.jpg", alt: "屋宇细节" }
                ],
                importance: 9,
                relatedWorks: [2, 3, 4, 5]
            },
            {
                id: 2,
                title: "黄山汤口",
                year: "1955",
                artist: "黄宾虹",
                size: "纵171厘米，横96厘米",
                material: "纸本水墨",
                collection: "中国美术馆",
                period: "晚年代表作",
                description: "黄宾虹晚年山水画的巅峰之作，描绘黄山汤口奇秀景色。作品以泼墨技法为主，笔墨浑厚华滋，将黄山的雄伟险峻与云雾缭绕表现得淋漓尽致。画面构图宏大，气势磅礴，展现了黄宾虹'黑、密、厚、重'的艺术风格。",
                tags: ["黄山题材", "泼墨", "晚年精品", "大型山水"],
                techniques: ["泼墨法", "积墨法", "焦墨法"],
                features: [
                    "泼墨技法的大胆运用",
                    "笔墨浑厚华滋，气势磅礴",
                    "构图宏大，层次分明",
                    "云雾表现生动自然",
                    "意境深远，富有诗意"
                ],
                analysis: [
                    {
                        title: "泼墨技法",
                        icon: "fas fa-water",
                        content: "大胆运用泼墨技法，墨色淋漓，气韵生动，形成独特的艺术效果。"
                    },
                    {
                        title: "空间处理",
                        icon: "fas fa-layer-group",
                        content: "采用深远法构图，前后层次分明，空间感强烈。"
                    },
                    {
                        title: "意境营造",
                        icon: "fas fa-cloud",
                        content: "通过云雾的巧妙处理，营造出黄山神秘而雄伟的意境。"
                    }
                ],
                images: [
                    { type: "main", src: "./images/huangshan.jpg", alt: "黄宾虹《黄山汤口》" },
                    { type: "detail", src: "./images/detail1.jpg", alt: "山石细节" }
                ],
                importance: 10,
                relatedWorks: [1, 3, 6]
            },
            
             {
                id: 3,
                title: "青城山中",
                year: "1953",
                artist: "黄宾虹",
                size: "纵136厘米，横68厘米",
                material: "纸本设色",
                collection: "上海博物馆",
                period: "晚年创作",
                description: "描绘青城山幽深景色的山水画作品。作品以干笔皴擦为主，湿笔渲染为辅，色墨交融，气象万千。画面中青城山的幽深静谧与道家仙气表现得恰到好处，体现了黄宾虹对自然景观的深刻理解和高超的艺术表现力。",
                tags: ["青城山", "干笔", "色墨交融", "道家意境"],
                techniques: ["干笔皴擦", "湿笔渲染", "设色法"],
                features: [
                    "干笔皴擦技法娴熟",
                    "色墨交融，层次丰富",
                    "构图严谨，疏密有致",
                    "意境幽深，富有仙气",
                    "笔墨苍劲有力"
                ],
                analysis: [
                    {
                        title: "干笔技法",
                        icon: "fas fa-pencil-alt",
                        content: "干笔皴擦技法娴熟，笔触苍劲有力，表现山石的质感。"
                    },
                    {
                        title: "色彩运用",
                        icon: "fas fa-fill-drip",
                        content: "色彩淡雅，与墨色完美融合，营造出青城山的幽深氛围。"
                    },
                    {
                        title: "意境表达",
                        icon: "fas fa-mountain",
                        content: "通过构图和笔墨表现青城山的道家仙气和幽深意境。"
                    }
                ],
                images: [
                    { type: "main", src: "./images/qingcheng.jpg", alt: "黄宾虹《青城山中》" }
                ],
                importance: 8,
                relatedWorks: [1, 2, 4]
            },
            
            {
                id: 4,
                title: "湖山清晓",
                year: "1948",
                artist: "黄宾虹",
                size: "纵89厘米，横47厘米",
                material: "纸本设色",
                collection: "故宫博物院",
                period: "晚年创作",
                description: "清晨湖山景色，墨色淋漓，层次丰富。作品描绘了湖光山色在清晨时分的清新与生机，画面中雾气朦胧，远山近水相映成趣，体现了黄宾虹对自然景色的敏锐观察和艺术表现力。",
                tags: ["湖山景色", "清晨", "墨色淋漓", "朦胧美"],
                techniques: ["渍墨法", "渍色法", "渲染法"],
                features: [
                    "墨色淋漓，层次丰富",
                    "雾气表现生动自然",
                    "构图简洁，意境清新",
                    "色彩淡雅，和谐统一",
                    "笔墨流畅，气韵生动"
                ],
                analysis: [
                    {
                        title: "雾气表现",
                        icon: "fas fa-smog",
                        content: "通过墨色的浓淡变化，生动表现清晨湖山的雾气朦胧。"
                    },
                    {
                        title: "色彩和谐",
                        icon: "fas fa-palette",
                        content: "色彩淡雅和谐，与墨色完美融合，营造清新意境。"
                    },
                    {
                        title: "空间营造",
                        icon: "fas fa-expand-arrows-alt",
                        content: "通过远近虚实处理，营造出深远的空间感。"
                    }
                ],
                images: [
                    { type: "main", src: "./images/hushan.jpg", alt: "黄宾虹《湖山清晓》" }
                ],
                importance: 7,
                relatedWorks: [1, 3, 5]
            },
            
            {
                id: 5,
                title: "溪山深处",
                year: "1952",
                artist: "黄宾虹",
                size: "纵102厘米，横52厘米",
                material: "纸本水墨",
                collection: "南京博物院",
                period: "晚年创作",
                description: "以干笔皴擦表现溪山深处景色，笔墨苍劲，意境幽深。作品描绘了深山溪流的静谧景象，画面构图严谨，笔墨老辣，展现了黄宾虹晚年艺术风格的成熟与自信。",
                tags: ["溪山", "干笔", "苍劲", "幽深意境"],
                techniques: ["干笔皴擦", "焦墨法", "积墨法"],
                features: [
                    "干笔皴擦技法纯熟",
                    "笔墨苍劲有力",
                    "构图严谨，层次分明",
                    "意境幽深静谧",
                    "黑白对比强烈"
                ],
                analysis: [
                    {
                        title: "干笔运用",
                        icon: "fas fa-pen-nib",
                        content: "干笔皴擦技法纯熟，笔触苍劲有力，表现山石的质感。"
                    },
                    {
                        title: "黑白对比",
                        icon: "fas fa-adjust",
                        content: "强烈的黑白对比，增强了画面的视觉冲击力。"
                    },
                    {
                        title: "意境营造",
                        icon: "fas fa-tree",
                        content: "通过构图和笔墨，营造出溪山深处的幽深静谧意境。"
                    }
                ],
                images: [
                    { type: "main", src: "./images/xishan.jpg", alt: "黄宾虹《溪山深处》" }
                ],
                importance: 7,
                relatedWorks: [1, 4, 6]
            },
            
           {
                id: 6,
                title: "秋山图",
                year: "1935",
                artist: "黄宾虹",
                size: "纵78厘米，横42厘米",
                material: "纸本设色",
                collection: "浙江省博物馆",
                period: "中期作品",
                description: "秋日山景，色彩丰富，构图严谨。作品描绘了秋天的山林景色，色彩斑斓，构图严谨，体现了黄宾虹中期艺术风格的特点。",
                tags: ["秋景", "中期作品", "构图严谨", "色彩丰富"],
                techniques: ["设色法", "渲染法", "勾勒法"],
                features: [
                    "色彩丰富，秋意浓厚",
                    "构图严谨，布局合理",
                    "笔墨工整，技法纯熟",
                    "意境清新，富有诗意",
                    "色彩与墨色和谐统一"
                ],
                analysis: [
                    {
                        title: "色彩表现",
                        icon: "fas fa-leaf",
                        content: "丰富的色彩表现秋天的山林景色，色彩与墨色和谐统一。"
                    },
                    {
                        title: "构图特点",
                        icon: "fas fa-th",
                        content: "构图严谨，布局合理，体现了传统山水画的构图法则。"
                    },
                    {
                        title: "中期风格",
                        icon: "fas fa-history",
                        content: "体现了黄宾虹中期艺术风格的特点，工整严谨，色彩丰富。"
                    }
                ],
                images: [
                    { type: "main", src: "./images/qiushan.jpg", alt: "黄宾虹《秋山图》" }
                ],
                importance: 6,
                relatedWorks: [2, 3, 7]
            },
             {
                id: 7,
                title: "书法对联",
                year: "1940",
                artist: "黄宾虹",
                size: "纵138厘米，横34厘米 × 2",
                material: "纸本水墨",
                collection: "私人收藏",
                period: "中期书法",
                description: "行书对联，笔力遒劲，结体严谨。作品体现了黄宾虹深厚的书法功底和独特的艺术风格，笔墨老辣，气韵生动。",
                tags: ["书法", "对联", "行书", "笔墨老辣"],
                techniques: ["中锋用笔", "侧锋取势", "浓淡对比"],
                features: [
                    "笔力遒劲，力透纸背",
                    "结体严谨，布局合理",
                    "笔墨老辣，气韵生动",
                    "章法自然，富有变化",
                    "传统功底深厚"
                ],
                analysis: [
                    {
                        title: "笔法特点",
                        icon: "fas fa-pen",
                        content: "中锋用笔为主，侧锋取势，笔力遒劲，力透纸背。"
                    },
                    {
                        title: "结体布局",
                        icon: "fas fa-text-height",
                        content: "结体严谨，布局合理，章法自然，富有变化。"
                    },
                    {
                        title: "书法风格",
                        icon: "fas fa-signature",
                        content: "体现了黄宾虹独特的书法风格，传统功底深厚，个性鲜明。"
                    }
                ],
                images: [
                    { type: "main", src: "./images/calligraphy1.jpg", alt: "黄宾虹《书法对联》" }
                ],
                importance: 5,
                relatedWorks: [6, 8, 10]
            },
          ]
        }
  },

}
</script>
